package com.mcherb.sb.zippingreader;

import com.mcherb.sb.zippingreader.model.RawCalendar;
import com.mcherb.sb.zippingreader.model.RawCompleteCalendar;
import com.mcherb.sb.zippingreader.model.RawExceptionDate;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.util.Collection;
import java.util.List;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public ItemReader<RawExceptionDate> calendarExceptionDatesReader() {
        BeanWrapperFieldSetMapper<RawExceptionDate> mapper = new BeanWrapperFieldSetMapper<>() {
        };
        mapper.setTargetType(RawExceptionDate.class);

        return new FlatFileItemReaderBuilder<RawExceptionDate>()
                .name("calendarExceptionDatesReader")
                .resource(new ClassPathResource("calendar_dates.txt"))
                .linesToSkip(1)
                .delimited()
                .delimiter(",")
                .names("id", "date", "exceptionType")
                .fieldSetMapper(mapper)
                .build();
    }

    @Bean
    public InMemoryRemovalSearchReader<String, RawExceptionDate> inMemorySearchReader() {
        InMemoryRemovalSearchReader<String, RawExceptionDate> inMemoryRemovalSearchReader = new InMemoryRemovalSearchReader<>();
        inMemoryRemovalSearchReader.setDelegate(calendarExceptionDatesReader());
        return inMemoryRemovalSearchReader;
    }

    @Bean
    public ItemReader<RawCalendar> calendarReader() {
        BeanWrapperFieldSetMapper<RawCalendar> mapper = new BeanWrapperFieldSetMapper<>() {
        };
        mapper.setTargetType(RawCalendar.class);

        return new FlatFileItemReaderBuilder<RawCalendar>()
                .name("calendarReader")
                .linesToSkip(1)
                .resource(new ClassPathResource("calendar.txt"))
                .delimited()
                .delimiter(",")
                .names("id", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday", "start_date", "end_date")
                .fieldSetMapper(mapper)
                .build();

    }

    @Bean
    public ZippingReader<String, RawCalendar, RawExceptionDate, RawCompleteCalendar> zippingReader() {
        ZippingReader<String, RawCalendar, RawExceptionDate, RawCompleteCalendar> zippingReader
                = new ZippingReader<>(RawCompleteCalendar::new);

        zippingReader.setDelegate(calendarReader());
        zippingReader.setSearchReader(inMemorySearchReader());
        return zippingReader;
    }


    @Bean
    public ItemWriter<List<RawExceptionDate>> calendarDateGtfInMemorysWriter() {
        return aggregates -> aggregates.stream()
                .flatMap(Collection::stream)
                .forEach(System.out::println);
    }

    @Bean
    public ItemWriter<RawCompleteCalendar> calendarWriter() {
        return calendars -> calendars.forEach(System.out::println);
    }

    @Bean
    public Step stepCompleteCalendar() {
        return stepBuilderFactory.get("completeCalendar")
                .<RawCompleteCalendar, RawCompleteCalendar>chunk(5)
                .reader(zippingReader())
                .writer(calendarWriter())
                .build();
    }

    @Bean
    @SuppressWarnings({"unchecked"})
    public Step stepExceptionDatesCalendar() {
        return stepBuilderFactory.get("exceptionDatesCalendar")
                .<RawExceptionDate, RawExceptionDate>chunk(5)
                .reader((ItemReader) inMemorySearchReader())
                .writer(calendarDateGtfInMemorysWriter())
                .build();
    }

    @Bean
    public Job job() {
        return jobBuilderFactory.get("job")
                .start(stepCompleteCalendar())
                .next(stepExceptionDatesCalendar())
                .build();
    }
}
