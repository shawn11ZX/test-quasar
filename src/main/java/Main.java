import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.SuspendableRunnable;
import mockito.FiberMockito;
import org.mockito.Mockito;

import java.util.concurrent.ExecutionException;

public class Main {


	public static void main(String[] args) throws InterruptedException, ExecutionException {

		final Callback cb = Mockito.mock(Callback.class);

		Fiber<Void> f = new Fiber<Void>(
				new SuspendableRunnable() {
					public void run() throws SuspendExecution, InterruptedException {

						Mockito.verify(cb, FiberMockito.timeout(2000)).callback();
						System.out.print("ok");
					}
				}
		);

		f.start();

		f.join();
	}
}