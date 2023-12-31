package dataAccess.UserProfile;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class database {
    /*Singleton pattern*/
    private static database instance = null;

    public static database getInstance() {
        if (instance == null) {
            instance = new database();
            main(null);
        }
        return instance;
    }

    /*database creation*/
    public static void main(String[] args) {
        String url = "jdbc:sqlite:Nutrifit/src/dataAccess/UserProfile/User.db";
        createDatabase(url);
        //uncomment to wipe DB on startup
        //wipeDB(url);
        initializeDatabase(url);
    }

    private static void wipeDB(String url) {
        // Wipe the entire database
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                Statement statement = conn.createStatement();
                statement.execute("PRAGMA writable_schema = 1;");
                statement.execute("delete from sqlite_master where type in ('table', 'index', 'trigger');");
                statement.execute("PRAGMA writable_schema = 0;");
                statement.execute("VACUUM;");
                statement.execute("PRAGMA INTEGRITY_CHECK;");
                System.out.println("User database wiped. Driver:" + meta.getDriverName());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void createDatabase(String url) {
        // Create the user database
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("User database created. Driver:" + meta.getDriverName());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void initializeDatabase(String url) {
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                Statement statement = conn.createStatement();
                // Create UserProfile table
                statement.execute("CREATE TABLE IF NOT EXISTS UserProfile (username TEXT, age INTEGER, sex TEXT, height_cm FLOAT, weight_kg FLOAT, PRIMARY KEY (username));");
                // Create DietLog table
                statement.execute("CREATE TABLE IF NOT EXISTS DietLog (ID INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, Date DATE, Meal_Type TEXT, Food_item TEXT, FoodID INTEGER, Quantity INTEGER, FOREIGN KEY (username) REFERENCES UserProfile(username));");
                // Create ExerciseLog table
                statement.execute("CREATE TABLE IF NOT EXISTS ExerciseLog (ID INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, Date DATE, Exercise_Type TEXT, Duration INTEGER, Intensity INTEGER, FOREIGN KEY (username) REFERENCES UserProfile(username));");
                System.out.println("User database initialized. Driver:" + meta.getDriverName());
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private Connection connect() throws SQLException {
        // SQLite connection string
        String url = "jdbc:sqlite:Nutrifit/src/dataAccess/UserProfile/User.db";
        Connection conn = null;
        conn = DriverManager.getConnection(url);
        return conn;
    }

    /*data insertion*/
    public void insertUserProfile(String name, int age, String sex, double height_cm, double weight_kg) {
        // Insert a new user profile into the UserProfile table
        StringBuilder sb = new StringBuilder();
        try (Connection conn = this.connect()) {
            sb.setLength(0);
            sb.append("INSERT INTO UserProfile(username, age, sex, height_cm, weight_kg) VALUES ('").append(name).append("', ").append(age).append(", '").append(sex).append("', ").append(height_cm).append(", ").append(weight_kg).append(");");
            Statement statement = conn.createStatement();
            System.out.println(sb);
            statement.executeUpdate(sb.toString());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void insertDietLog(String username, Date date, String mealType, String foodItem, int foodID, int quantity) {
        // Insert a new diet log into the DietLog table
        String sql = "INSERT INTO DietLog(username, Date, Meal_Type, Food_item, FoodID, Quantity) VALUES(?,?,?,?,?,?)";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setDate(2, date);
            pstmt.setString(3, mealType);
            pstmt.setString(4, foodItem);
            pstmt.setInt(5, foodID);
            pstmt.setInt(6, quantity);
            pstmt.executeUpdate();
            System.out.println("A new diet log has been inserted.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public int insertExerciseLog(String username, Date date, String exerciseType, int duration, int intensity) {
        // Insert a new exercise log into the ExerciseLog table
        String sql = "INSERT INTO ExerciseLog(username, Date, Exercise_Type, Duration, Intensity) VALUES(?,?,?,?,?)";
        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setDate(2, date);
            pstmt.setString(3, exerciseType);
            pstmt.setInt(4, duration);
            pstmt.setInt(5, intensity);
            pstmt.executeUpdate();
            System.out.println("A new exercise log has been inserted.");
            return 1;
        } catch (SQLException e) {
            System.out.println("weewoo");
            System.out.println(e.getMessage());
            return -1;
        }
    }

    /*data retrieval*/
    //personal
    public ArrayList<String> listProfiles() {
        // List all user profiles
        ArrayList<String> profiles = new ArrayList<>();
        String sql = "SELECT username FROM UserProfile";

        try (Connection conn = this.connect(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {

            // Loop through the result set
            while (rs.next()) {
                profiles.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return profiles;
    }

    public ArrayList<String> getUserProfile(String username) {
        ArrayList<String> profile = new ArrayList<>();
        String sql = "select * from UserProfile where username = ?";
        try (Connection conn = this.connect()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                profile.add(rs.getString("username"));
                profile.add(rs.getString("age"));
                profile.add(rs.getString("sex"));
                profile.add(rs.getString("height_cm"));
                profile.add(rs.getString("weight_kg"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return profile;
    }

    public double getWeight(String username) {
        double weight = 0;
        String sql = "SELECT weight_kg FROM UserProfile WHERE username is ?";
        try (Connection conn = this.connect()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            weight = rs.getDouble("weight_kg");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return weight;
    }


    //food
    public HashMap<Date, ArrayList<Integer>> getFoodIDs(String username) {
        HashMap<Date, ArrayList<Integer>> foodIDs = new HashMap<>();
        String sql = "SELECT Date, FoodID, Quantity FROM DietLog WHERE username is ?";
        try (Connection conn = this.connect()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<Integer> foodID = new ArrayList<>();
            while (rs.next()) {
                for (int i = 0; i < rs.getInt("Quantity"); i++) {
                    foodID.add(rs.getInt("FoodID"));
                }
                foodIDs.put(rs.getDate("Date"), foodID);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("getFoodID fin: " + foodIDs);
        return foodIDs;
    }

    public HashMap<Date, ArrayList<Integer>> getFoodIDs(String username, int days) {
        //get all entrys with a Date within the last int days
        HashMap<Date, ArrayList<Integer>> foodIDs = new HashMap<>();
        String sql = "SELECT Date, FoodID, Quantity FROM DietLog WHERE username is ? AND Date > ?";
        ArrayList<Integer> foodID = new ArrayList<>();
        try (Connection conn = this.connect()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setDate(2, new Date(System.currentTimeMillis() - days * 86400000L));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                for (int i = 0; i < rs.getInt("Quantity"); i++) {
                    foodID.add(rs.getInt("FoodID"));
                }
                foodIDs.put(rs.getDate("Date"), foodID);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return foodIDs;
    }

    public double dailyIntake(String username, Date date) {
        double dailyIntake = 0;
        String sql = "SELECT FoodID, Quantity FROM DietLog WHERE Date is ? and username is ?";
        try (Connection conn = this.connect()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, date);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                dailyIntake += dataAccess.nutritionData.database.getInstance().getCalories(rs.getInt("FoodID")) * rs.getInt("Quantity");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return dailyIntake;
    }

    //exercise
    public HashMap<Date, ArrayList<Integer>> getExerciseIDs(String username) {
        HashMap<Date, ArrayList<Integer>> exerciseIDs = new HashMap<>();
        String sql = "SELECT Date, ID FROM ExerciseLog WHERE username is ?";
        try (Connection conn = this.connect()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            ArrayList<Integer> exerciseID = new ArrayList<>();
            while (rs.next()) {
                exerciseID.add(rs.getInt("ID"));
                exerciseIDs.put(rs.getDate("Date"), exerciseID);
            }
        } catch (SQLException e) {
            System.out.println("HERE: "+e.getMessage());
        }
        return exerciseIDs;
    }

    public ArrayList<String> getExercise(String username, int exerciseId) {
        // Get an exercise log
        ArrayList<String> exercise = new ArrayList<>();
        String sql = "SELECT Date, Exercise_Type, Duration, Intensity FROM ExerciseLog WHERE username = ? AND ID = ?";

        try (Connection conn = this.connect()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setInt(2, exerciseId);
            ResultSet rs = pstmt.executeQuery();

            // Loop through the result set
            while (rs.next()) {
                exercise.add(rs.getString("Date"));
                exercise.add(rs.getString("Exercise_Type"));
                exercise.add(rs.getString("Duration"));
                exercise.add(rs.getString("Intensity"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return exercise;
    }

    public int getExerciseIntensity(int exerciseID) {
        int intensity = 0;
        String sql = "SELECT Intensity FROM ExerciseLog WHERE ID is ?";
        try (Connection conn = this.connect()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, exerciseID);
            ResultSet rs = pstmt.executeQuery();
            intensity = rs.getInt("Intensity");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return intensity;
    }

    public double getExerciseDuration(int exerciseID) {
        double duration = 0;
        String sql = "SELECT Duration FROM ExerciseLog WHERE ID is ?";
        try (Connection conn = this.connect()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, exerciseID);
            ResultSet rs = pstmt.executeQuery();
            duration = rs.getDouble("Duration");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return duration;
    }

    public double calorieBurned(String username, int exerciseID) {
        double burn = switch (database.getInstance().getExerciseIntensity(exerciseID)) {
            case 1 ->
                    (database.getInstance().getExerciseDuration(exerciseID) * 2 * database.getInstance().getWeight(username)) / 200;
            case 2 ->
                    (database.getInstance().getExerciseDuration(exerciseID) * 5 * database.getInstance().getWeight(username)) / 200;
            case 3 ->
                    (database.getInstance().getExerciseDuration(exerciseID) * 8 * database.getInstance().getWeight(username)) / 200;
            case 4 ->
                    (database.getInstance().getExerciseDuration(exerciseID) * 10 * database.getInstance().getWeight(username)) / 200;
            default -> -1;
        };
        System.out.println("calorieBurned: " + burn);
        return burn;
    }

    public double dailyBurn(String username, Date date) {
        double dailyBurn = 0;
        String sql = "SELECT ID FROM ExerciseLog WHERE Date is ? and username is ?";
        try (Connection conn = this.connect()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, date);
            pstmt.setString(2, username);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                dailyBurn += calorieBurned(username, rs.getInt("ID"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return dailyBurn;
    }

    /*data modification*/
    public void deleteUserProfile(String username) {
        // Delete a user profile
        String sql = "DELETE FROM UserProfile WHERE username = ?";
        try (Connection conn = this.connect()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void setAge(String username, int newAge) {
        String sql = "UPDATE UserProfile SET age = ? WHERE username = ?";
        try (Connection conn = this.connect()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, newAge);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void setSex(String username, String sex) {
        String sql = "UPDATE UserProfile SET sex = ? WHERE username = ?";
        try (Connection conn = this.connect()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, sex);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void setHeight(String username, double height) {
        String sql = "UPDATE UserProfile SET height_cm = ? WHERE username = ?";
        try (Connection conn = this.connect()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, height);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void setWeight(String username, double weight) {
        String sql = "UPDATE UserProfile SET weight_kg = ? WHERE username = ?";
        try (Connection conn = this.connect()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, weight);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}