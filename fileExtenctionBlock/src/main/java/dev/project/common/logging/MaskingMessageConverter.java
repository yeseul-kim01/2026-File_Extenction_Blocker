package dev.project.common.logging;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class MaskingMessageConverter extends MessageConverter {
    @Override
    public String convert(ILoggingEvent event) {
        String msg = super.convert(event);
        return LogMasker.mask(msg);
    }
}
