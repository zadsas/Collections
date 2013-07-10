package collections;

import java.io.File;

public class MyFileCollectionTest {

	public static void main(String[] args) {
		new MyFileCollectionTest().test();
	}

	private void test() {
		for (File f : new MyFileCollection("C:")) {
			System.out.println(f);// If this is time consuming, then better to do this than load 100000 File objs in memory
		}
	}
}
