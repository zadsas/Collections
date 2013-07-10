package collections;

import java.io.File;
import java.io.FileFilter;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

public abstract class UnmodifiableOnDemandFileCollection extends AbstractCollection<File> implements Collection<File> {

	private int size;

	private File dir;

	protected abstract boolean fileIsAccepted(File f);

	public UnmodifiableOnDemandFileCollection(final String dir) {
		this.dir = new File(dir);
		this.size = countFiles(this.dir);
	}

	private int countFiles(File directory) {
		int counter = 0;
		for (File file : directory.listFiles()) {
			if (fileIsAccepted(file))
				counter++;
		}
		return counter;
	}

	@Override
	public Iterator<File> iterator() {
		return new Iterator<File>() {

			private final Semaphore semaphore = new Semaphore(0);
			private final CyclicBarrier barrier = new CyclicBarrier(2);
			private final AtomicReference<File> ref = new AtomicReference<File>();

			{
				final Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						UnmodifiableOnDemandFileCollection.this.dir.listFiles(new FileFilter() {

							@Override
							public boolean accept(final File f) {
								if (fileIsAccepted(f)) {
									ref.set(f);
									semaphore.release();
									try {
										barrier.await();
									} catch (final Exception e) {
										throw new RuntimeException(e);
									}
								}
								return false;
							}

						});
						semaphore.release();
					}

				});
				t.setDaemon(true);
				t.start();
			}

			@Override
			public boolean hasNext() {
				semaphore.acquireUninterruptibly();
				try {
					return ref.get() != null;
				} finally {
					semaphore.release();
				}

			}

			@Override
			public File next() {
				semaphore.acquireUninterruptibly();
				try {
					final File file = ref.getAndSet(null);
					if (file == null) {
						semaphore.release();
						throw new NoSuchElementException();
					}
					return file;
				} finally {
					try {
						barrier.await();
					} catch (final Exception e) {
						throw new RuntimeException(e);
					}
				}
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Item removal is not supported by this iterator");
			}
		};
	}

	@Override
	public int size() {
		return size;
	}

}
