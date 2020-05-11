import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;

public class populate {
    public static void main(String[] args) throws IOException, SQLException {
        DBConnection db = new DBConnection();
        db.DBConnect();

        String businessFileName = args[0];
        String reviewFileName = args[1];
        String checkinFileName = args[2];
        String userFileName = args[3];

        System.out.println(businessFileName +  "   " + reviewFileName +  "   "  + checkinFileName  +    "   " +  userFileName );
        JsonParsing jsonParsing = new JsonParsing();
        jsonParsing.createFile(businessFileName);
        jsonParsing.createFile(reviewFileName);
        jsonParsing.createFile(checkinFileName);
        jsonParsing.createFile(userFileName);


        JsonParsing.parseBusinessFile();
        System.out.println("Parsing Business file Completed");
        //JsonParsing.parseUserFile();
        System.out.println("Parsing User file Completed");
        //JsonParsing.parseReviewFile();
        System.out.println("Parsing Review file Completed");

        db.insertQueriesInDb();
    }
}
