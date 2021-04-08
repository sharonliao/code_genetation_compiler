import java.util.ArrayList;
import java.util.Arrays;

public class test {
    public static void main(String[] args){
        ArrayList<String> listOne = new ArrayList<>(Arrays.asList("a", "b", "c", "d", "f"));

        ArrayList<String> listTwo = new ArrayList<>(Arrays.asList("a", "b", "c", "d", "e"));

        ArrayList<String> list3 = new ArrayList<>(Arrays.asList( "b","a", "c", "d", "e"));

        ArrayList<String> list4 = new ArrayList<>(Arrays.asList( "b","a", "c", "d", "e"));

        System.out.println(listOne.equals(listTwo));
        System.out.println(listTwo.equals(list3));
        System.out.println(list3.equals(list4));


    }
}
