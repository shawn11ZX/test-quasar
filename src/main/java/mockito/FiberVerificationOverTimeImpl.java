package mockito;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.internal.verification.AtMost;
import org.mockito.internal.verification.NoMoreInteractions;
import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.verification.VerificationMode;

/**
 * Created by Administrator on 2016/11/28.
 */
public class FiberVerificationOverTimeImpl implements VerificationMode {
    private final long pollingPeriodMillis;
    private final long durationMillis;
    private final VerificationMode delegate;
    private final boolean returnOnSuccess;


    /**
     * Create this verification mode, to be used to verify invocation ongoing data later.
     *
     * @param pollingPeriodMillis The frequency to poll delegate.verify(), to check whether the delegate has been satisfied
     * @param durationMillis The max time to wait (in millis) for the delegate verification mode to be satisfied
     * @param delegate The verification mode to delegate overall success or failure to
     * @param returnOnSuccess Whether to immediately return successfully once the delegate is satisfied (as in
     *                        {@link org.mockito.verification.VerificationWithTimeout}, or to only return once
     *                        the delegate is satisfied and the full duration has passed (as in
     *                        {@link org.mockito.verification.VerificationAfterDelay}).
     */
    public FiberVerificationOverTimeImpl(long pollingPeriodMillis, long durationMillis, VerificationMode delegate, boolean returnOnSuccess) {
        this(pollingPeriodMillis, delegate, returnOnSuccess, durationMillis);
    }

    /**
     * Create this verification mode, to be used to verify invocation ongoing data later.
     *
     * @param pollingPeriodMillis The frequency to poll delegate.verify(), to check whether the delegate has been satisfied
     * @param delegate The verification mode to delegate overall success or failure to
     * @param returnOnSuccess Whether to immediately return successfully once the delegate is satisfied (as in
     *                        {@link org.mockito.verification.VerificationWithTimeout}, or to only return once
     *                        the delegate is satisfied and the full duration has passed (as in
     *                        {@link org.mockito.verification.VerificationAfterDelay}).

     */
    public FiberVerificationOverTimeImpl(long pollingPeriodMillis, VerificationMode delegate, boolean returnOnSuccess, long durationMillis) {
        this.pollingPeriodMillis = pollingPeriodMillis;
        this.delegate = delegate;
        this.returnOnSuccess = returnOnSuccess;
        this.durationMillis = durationMillis;
    }

    /**
     * Verify the given ongoing verification data, and confirm that it satisfies the delegate verification mode
     * before the full duration has passed.
     *
     * In practice, this polls the delegate verification mode until it is satisfied. If it is not satisfied once
     * the full duration has passed, the last error returned by the delegate verification mode will be thrown
     * here in turn. This may be thrown early if the delegate is unsatisfied and the verification mode is known
     * to never recover from this situation (e.g. {@link AtMost}).
     *
     * If it is satisfied before the full duration has passed, behaviour is dependent on the returnOnSuccess parameter
     * given in the constructor. If true, this verification mode is immediately satisfied once the delegate is. If
     * false, this verification mode is not satisfied until the delegate is satisfied and the full time has passed.
     *
     * @throws MockitoAssertionError if the delegate verification mode does not succeed before the timeout
     */
    @Suspendable
    public void verify(VerificationData data) {
        AssertionError error = null;

        try {
            long start = System.currentTimeMillis();
            while ((System.currentTimeMillis() - start) < durationMillis) {
                try {
                    delegate.verify(data);

                    if (returnOnSuccess) {
                        return;
                    } else {
                        error = null;
                    }


                } catch (MockitoAssertionError e) {
                    error = handleVerifyException(e);
                } catch (AssertionError e) {
                    error = handleVerifyException(e);
                }
            }

            if (error != null) {
                throw error;
            }
        } catch (SuspendExecution se)
        {

        }
    }

    private AssertionError handleVerifyException(AssertionError e) throws SuspendExecution {
        if (canRecoverFromFailure(delegate)) {
            sleep(pollingPeriodMillis);
            return e;
        } else {
            throw e;
        }
    }

    protected boolean canRecoverFromFailure(VerificationMode verificationMode) {
        return !(verificationMode instanceof AtMost || verificationMode instanceof NoMoreInteractions);
    }

    public FiberVerificationOverTimeImpl copyWithVerificationMode(VerificationMode verificationMode) {
        return new FiberVerificationOverTimeImpl(pollingPeriodMillis, durationMillis, verificationMode, returnOnSuccess);
    }


    private void sleep(long sleep) throws SuspendExecution {
        try {
            Strand.sleep(sleep);
        } catch (InterruptedException ie) {
            throw new RuntimeException("Thread sleep has been interrupted", ie);
        }
    }

    @Override
    public VerificationMode description(String description) {
        return VerificationModeFactory.description(this, description);
    }

    public boolean isReturnOnSuccess() {
        return returnOnSuccess;
    }

    public long getPollingPeriodMillis() {
        return pollingPeriodMillis;
    }


    public VerificationMode getDelegate() {
        return delegate;
    }
}
