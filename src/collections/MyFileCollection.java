package collections;

import java.io.File;

public class MyFileCollection extends UnmodifiableOnDemandFileCollection {

	public MyFileCollection(final String dir) {
		super(dir);
	}

	@Override
	protected boolean fileIsAccepted(File f) {
		return true;
	}
}
