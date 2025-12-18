package com.audition.common.logging;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.ProblemDetail;

@ExtendWith(MockitoExtension.class)
class AuditionLoggerTest {


    @Mock
    @SuppressFBWarnings("URF_UNREAD_FIELD")
    @SuppressWarnings("PMD.LoggerIsNotStaticFinal")
    private transient Logger logger;

    private transient AuditionLogger auditionLogger;

    @BeforeEach
    void setUp() {

        auditionLogger = new AuditionLogger();

    }

    @Test
    void info_shouldLog_whenInfoEnabled() {
        when(logger.isInfoEnabled()).thenReturn(true);

        auditionLogger.info(logger, "test message");

        verify(logger).info("test message");
    }

    @Test
    void info_shouldNotLog_whenInfoDisabled() {
        when(logger.isInfoEnabled()).thenReturn(false);

        auditionLogger.info(logger, "test message");

        verify(logger, never()).info(anyString());
    }

    @Test
    void info_withObject_shouldLog_whenInfoEnabled() {
        when(logger.isInfoEnabled()).thenReturn(true);

        auditionLogger.info(logger, "message {}", "value");

        verify(logger).info("message {}", "value");
    }

    @Test
    void debug_shouldLog_whenDebugEnabled() {
        when(logger.isDebugEnabled()).thenReturn(true);

        auditionLogger.debug(logger, "debug message");

        verify(logger).debug("debug message");
    }

    @Test
    void warn_shouldLog_whenWarnEnabled() {
        when(logger.isWarnEnabled()).thenReturn(true);

        auditionLogger.warn(logger, "warn message");

        verify(logger).warn("warn message");
    }

    @Test
    void error_shouldLog_whenErrorEnabled() {
        when(logger.isErrorEnabled()).thenReturn(true);

        auditionLogger.error(logger, "error message");

        verify(logger).error("error message");
    }

    @Test
    void logErrorWithException_shouldLogException_whenErrorEnabled() {
        when(logger.isErrorEnabled()).thenReturn(true);
        Exception ex = new RuntimeException("boom");

        auditionLogger.logErrorWithException(logger, "error occurred", ex);

        verify(logger).error("error occurred", ex);
    }

    @Test
    void logStandardProblemDetail_shouldLogFormattedMessage() {
        when(logger.isErrorEnabled()).thenReturn(true);
        final Exception ex = new RuntimeException("failure");

        ProblemDetail problemDetail = ProblemDetail.forStatus(400);
        problemDetail.setTitle("Bad Request");
        problemDetail.setDetail("Invalid input");
        problemDetail.setType(URI.create("https://sampleposts.com/problem"));
        problemDetail.setInstance(URI.create("/test"));

        auditionLogger.logStandardProblemDetail(logger, problemDetail, ex);

        verify(logger).error(
            contains("type: https://sampleposts.com/problem"),
            eq(ex)
        );
    }

    @Test
    void logHttpStatusCodeError_shouldLogFormattedErrorMessage() {
        when(logger.isErrorEnabled()).thenReturn(true);

        auditionLogger.logHttpStatusCodeError(logger, "Something failed", 500);

        verify(logger).error("status: 500, message: Something failed\n");
    }


}
