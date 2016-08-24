package fundamentals;

import org.openjdk.jol.info.GraphLayout;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.util.VMSupport;

public class TestMemoryLayout {

  public static void main(String[] args) {
    new Container().printMe();

    System.out.println(VMSupport.vmDetails());
    System.out.println(ClassLayout.parseClass(Container.class).toPrintable());
    System.out.println(GraphLayout.parseInstance(new Container()).toPrintable());

  }

  private static class Container {

    private final int[] constantContent = new int[] {1, 2, 3, 4};

    public void printMe() {
      for (int i = 0; i < constantContent.length; i++) {
        System.out.println(constantContent[i]);
      }
    }

  }

}
