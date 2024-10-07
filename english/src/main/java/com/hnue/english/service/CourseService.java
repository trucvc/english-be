package com.hnue.english.service;

import com.hnue.english.model.Course;
import com.hnue.english.model.Topic;
import com.hnue.english.reponsitory.CourseReponsitory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseReponsitory courseReponsitory;

    public void createCourse(Course theCourse){
        courseReponsitory.save(theCourse);
    }

    public Course getCourse(int id){
        Course course = courseReponsitory.getCourseWithTopic(id).orElseThrow(()-> new DateTimeException("Không tồn tại khóa học với id: "+id));

        List<Topic> theTopic = new ArrayList<>(course.getTopics());
        theTopic.sort(Comparator.comparing(Topic::getOrder));

        course.setTopics(new ArrayList<>(theTopic));
        return course;
    }

    public List<Course> getAllCourse(){
        return courseReponsitory.findAll();
    }

    public Course updateCourse(int id, Course theCourse){
        Course course = courseReponsitory.findById(id).orElseThrow(() -> new DateTimeException("Không tồn tại khóa học với id: "+id));

        course.setCourseName(theCourse.getCourseName());
        course.setDescription(theCourse.getDescription());

        return courseReponsitory.save(course);
    }

    public void deleteCourse(int id){
        Course course = courseReponsitory.findById(id).orElseThrow(() -> new DateTimeException("Không tồn tại khóa học với id: "+id));
        for (Topic theTopic : course.getTopics()){
            theTopic.setCourse(null);
        }
        courseReponsitory.delete(course);
    }
}
