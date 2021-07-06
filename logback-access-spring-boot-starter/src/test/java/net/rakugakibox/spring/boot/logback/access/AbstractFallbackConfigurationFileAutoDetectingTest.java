package net.rakugakibox.spring.boot.logback.access;

import net.rakugakibox.spring.boot.logback.access.test.LogbackAccessEventQueuingAppenderRule;
import net.rakugakibox.spring.boot.logback.access.test.LogbackAccessEventQueuingListener;
import net.rakugakibox.spring.boot.logback.access.test.LogbackAccessEventQueuingListenerConfiguration;
import net.rakugakibox.spring.boot.logback.access.test.LogbackAccessEventQueuingListenerRule;
import net.rakugakibox.spring.boot.logback.access.test.TestControllerConfiguration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.OutputCaptureRule;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static net.rakugakibox.spring.boot.logback.access.test.ResponseEntityAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The base class for testing to auto detect fallback configuration file.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractFallbackConfigurationFileAutoDetectingTest {

    /**
     * The output capture rule.
     */
    @Rule
    public final OutputCaptureRule outputCapture = new OutputCaptureRule();

    /**
     * The REST template.
     */
    @Autowired
    protected TestRestTemplate rest;

    /**
     * Creates a test rule.
     *
     * @return a test rule.
     */
    @Rule
    public TestRule rule() {
        return RuleChain
                .outerRule(new LogbackAccessEventQueuingAppenderRule())
                .around(new LogbackAccessEventQueuingListenerRule())
                .around(outputCapture);
    }

    /**
     * Tests a Logback-access event.
     */
    @Test
    public void logbackAccessEvent() {

        ResponseEntity<String> response = rest.getForEntity("/test/text", String.class);
        LogbackAccessEventQueuingListener.appendedEventQueue.pop();

        assertThat(response).hasStatusCode(HttpStatus.OK);
        assertThat(outputCapture.toString()).containsSequence("127.0.0.1", "GET", "/test/text", "HTTP/1.1", "200");

    }

    /**
     * The base class of context configuration.
     */
    @EnableAutoConfiguration(exclude = SecurityAutoConfiguration.class)
    @Import({LogbackAccessEventQueuingListenerConfiguration.class, TestControllerConfiguration.class})
    public static abstract class AbstractContextConfiguration {
    }

}
