import org.springframework.stereotype.Service;

@Service

public class StudentService {

    public String getStudentById(int id) {

        return "Student{id=" + id + ", name='John Doe'}";

    }

    public void registerStudent(String name) {

        System.out.println("Registering student: " + name);

    }

}