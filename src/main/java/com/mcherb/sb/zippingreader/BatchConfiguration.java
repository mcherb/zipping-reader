package com.mcherb.sb.zippingreader;

import com.mcherb.sb.zippingreader.model.Calendar;
import com.mcherb.sb.zippingreader.model.RawExceptionDate;
import com.mcherb.sb.zippingreader.model.ExceptionDate;
import com.mcherb.sb.zippingreader.model.RawCalendar;
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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    private static final String CALENDAR_ID = "CAL:%s";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");


    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public ItemReader<RawExceptionDate> calendarDateGtfsReader() {
        BeanWrapperFieldSetMapper<RawExceptionDate> mapper = new BeanWrapperFieldSetMapper<>() {
        };
        mapper.setTargetType(RawExceptionDate.class);

        return new FlatFileItemReaderBuilder<RawExceptionDate>()
                .name("calendarDateGtfs")
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
        inMemoryRemovalSearchReader.setDelegate(calendarDateGtfsReader());
        return inMemoryRemovalSearchReader;
    }

    @Bean
    public ItemReader<RawCalendar> calendarGtfsReader() {
        BeanWrapperFieldSetMapper<RawCalendar> mapper = new BeanWrapperFieldSetMapper<>() {
        };
        mapper.setTargetType(RawCalendar.class);

        return new FlatFileItemReaderBuilder<RawCalendar>()
                .name("calendarGtfs")
                .linesToSkip(1)
                .resource(new ClassPathResource("calendar.txt"))
                .delimited()
                .delimiter(",")
                .names("id", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday", "start_date", "end_date")
                .fieldSetMapper(mapper)
                .build();

    }

    private List<ExceptionDate> buildExceptionDates(List<RawExceptionDate> calendarDates) {
        return calendarDates.stream().map(date -> {
            ExceptionDate exceptionDate = new ExceptionDate();
            exceptionDate.setDate(LocalDate.parse(date.getDate(), DATE_FORMATTER));
            exceptionDate.setCirculating(date.getExceptionType().equals("1"));
            return exceptionDate;
        }).collect(Collectors.toList());
    }

    @Bean
    public ZippingReader<String, RawCalendar, RawExceptionDate, Calendar> zippingReader() {
        ZippingReader<String, RawCalendar, RawExceptionDate, Calendar> zippingReader
                = new ZippingReader<>(
                (c, d) -> Calendar.builder()
                        .id(String.format(CALENDAR_ID, c.getId()))
                        .created(OffsetDateTime.now())
                        .exceptionDates(buildExceptionDates(d))
                        .build()
        );

        zippingReader.setDelegate(calendarGtfsReader());
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
    public ItemWriter<Calendar> calendarWriter() {
        return calendars -> calendars.forEach(System.out::println);
    }


    @Bean
    @SuppressWarnings({"unchecked"})
    public Step stepCalendarDateGtfs() {
        return stepBuilderFactory.get("readCalendarDateGtfs")
                .<RawExceptionDate, RawExceptionDate>chunk(5)
                .reader((ItemReader) inMemorySearchReader())
                .writer(calendarDateGtfInMemorysWriter())
                .build();
    }

    @Bean
    public Step stepCalendarGtfs() {
        return stepBuilderFactory.get("readCalendar")
                .<Calendar, Calendar>chunk(5)
                .reader(zippingReader())
                .writer(calendarWriter())
                .build();
    }

    @Bean
    public Job job() {
        return jobBuilderFactory.get("job")
                .start(stepCalendarGtfs())
                .next(stepCalendarDateGtfs())
                .build();
    }
}
