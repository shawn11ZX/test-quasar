package mockito;

import org.mockito.internal.verification.VerificationModeFactory;
import org.mockito.verification.VerificationWithTimeout;

/**
 * Created by Administrator on 2016/11/28.
 */
public class FiberMockito {
    public static VerificationWithTimeout timeout(long millis) {
        return new FilberTimeout(500, millis, VerificationModeFactory.times(1));
    }
}
