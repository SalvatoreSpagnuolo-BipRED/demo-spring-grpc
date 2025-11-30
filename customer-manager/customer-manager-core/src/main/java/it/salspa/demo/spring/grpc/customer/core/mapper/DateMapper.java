package it.salspa.demo.spring.grpc.customer.core.mapper;

import com.google.protobuf.Timestamp;
import org.mapstruct.Mapper;

import java.time.Instant;
import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface DateMapper {

    default Timestamp toTimestamp(Instant value) {
        if (value == null) {
            return null;
        }
        return Timestamp.newBuilder()
                        .setSeconds(value.getEpochSecond())
                        .setNanos(value.getNano())
                        .build();
    }

    default LocalDate toLocalDate(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos())
                      .atZone(java.time.ZoneId.systemDefault())
                      .toLocalDate();
    }

    default Timestamp toTimestamp(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        Instant instant = localDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
        return Timestamp.newBuilder()
                        .setSeconds(instant.getEpochSecond())
                        .setNanos(instant.getNano())
                        .build();
    }
}
