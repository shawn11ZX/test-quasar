package mockito;

/**
 * Created by Administrator on 2016/11/28.
 */
/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */

import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.internal.verification.VerificationWrapper;
import org.mockito.verification.VerificationMode;
import org.mockito.verification.VerificationWithTimeout;

import static org.mockito.internal.exceptions.Reporter.atMostAndNeverShouldNotBeUsedWithTimeout;

/**
 * See the javadoc for {@link VerificationWithTimeout}
 * <p>
 * Typically, you won't use this class explicitly. Instead use timeout() method on Mockito class.
 * See javadoc for {@link VerificationWithTimeout}
 */
public class FilberTimeout extends VerificationWrapper<FiberVerificationOverTimeImpl> implements VerificationWithTimeout {

    /**
     * See the javadoc for {@link VerificationWithTimeout}
     * <p>
     * Typically, you won't use this class explicitly. Instead use timeout() method on Mockito class.
     * See javadoc for {@link VerificationWithTimeout}
     */
    public FilberTimeout(long millis, VerificationMode delegate) {
        this(10, millis, delegate);
    }

    /**
     * See the javadoc for {@link VerificationWithTimeout}
     */
    FilberTimeout(long pollingPeriodMillis, long millis, VerificationMode delegate) {
        this(new FiberVerificationOverTimeImpl(pollingPeriodMillis, millis, delegate, true));
    }


    FilberTimeout(FiberVerificationOverTimeImpl verificationOverTime) {
        super(verificationOverTime);
    }

    @Override
    protected VerificationMode copySelfWithNewVerificationMode(VerificationMode newVerificationMode) {
        return new FilberTimeout(wrappedVerification.copyWithVerificationMode(newVerificationMode));
    }

    public VerificationMode atMost(int maxNumberOfInvocations) {
        throw atMostAndNeverShouldNotBeUsedWithTimeout();
    }

    public VerificationMode never() {
        throw atMostAndNeverShouldNotBeUsedWithTimeout();
    }

    @Override
    public VerificationMode description(String description) {
        return VerificationModeFactory.description(this, description);
    }

}

