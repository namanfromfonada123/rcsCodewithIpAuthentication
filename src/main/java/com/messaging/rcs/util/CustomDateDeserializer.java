package com.messaging.rcs.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Component
public class CustomDateDeserializer extends StdDeserializer<Date> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = org.apache.log4j.Logger.getLogger(CustomDateDeserializer.class.getName());
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // specify your specific timezone


    public CustomDateDeserializer() {
        this(null);
    }

    public CustomDateDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Date deserialize(JsonParser jsonparser, DeserializationContext context)
            throws IOException, JsonProcessingException {
        String date = jsonparser.getText();
        LOGGER.info("STR date:"+date);

        try {
            formatter.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
            Date date1 =  formatter.parse(date);
            LOGGER.info("Date is:"+date1);
            return date1;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}

