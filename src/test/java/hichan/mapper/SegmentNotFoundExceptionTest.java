package hichan.mapper;

public class SegmentNotFoundExceptionTest {

    public static void main(String[] args) {

        try {

            throw new SegmentNotFoundException(
                    "No matching segment found.");

        } catch (SegmentNotFoundException e) {

            System.out.println("Exception caught:");
            System.out.println(e.getMessage());
        }
    }
}