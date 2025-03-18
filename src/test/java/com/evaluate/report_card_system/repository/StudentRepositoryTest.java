package com.evaluate.report_card_system.repository;

import com.evaluate.report_card_system.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentRepositoryTest {

    @Mock
    private StudentRepository studentRepository;

    private Student sampleStudent;

    @BeforeEach
    void setUp() {
        sampleStudent = new Student();
        sampleStudent.setId("1");
        sampleStudent.setRollNumber(101);
        sampleStudent.setName("John Doe");
    }

    @Test
    void save_ShouldReturnSavedStudent() {

        when(studentRepository.save(sampleStudent)).thenReturn(sampleStudent);

        Student savedStudent = studentRepository.save(sampleStudent);

        assertNotNull(savedStudent);
        assertEquals("1", savedStudent.getId());
        assertEquals(101, savedStudent.getRollNumber());
        assertEquals("John Doe", savedStudent.getName());
        verify(studentRepository, times(1)).save(sampleStudent);
    }

    @Test
    void findById_ShouldReturnStudent_WhenExists() {

        when(studentRepository.findById("1")).thenReturn(Optional.of(sampleStudent));

        Optional<Student> foundStudent = studentRepository.findById("1");

        assertTrue(foundStudent.isPresent());
        assertEquals("1", foundStudent.get().getId());
        assertEquals(101, foundStudent.get().getRollNumber());
        verify(studentRepository, times(1)).findById("1");
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {

        when(studentRepository.findById("999")).thenReturn(Optional.empty());

        Optional<Student> foundStudent = studentRepository.findById("999");

        assertFalse(foundStudent.isPresent());
        verify(studentRepository, times(1)).findById("999");
    }

    @Test
    void findByRollNumber_ShouldReturnStudent_WhenExists() {

        when(studentRepository.findByRollNumber(101)).thenReturn(Optional.of(sampleStudent));

        Optional<Student> foundStudent = studentRepository.findByRollNumber(101);

        assertTrue(foundStudent.isPresent());
        assertEquals(101, foundStudent.get().getRollNumber());
        assertEquals("John Doe", foundStudent.get().getName());
        verify(studentRepository, times(1)).findByRollNumber(101);
    }

    @Test
    void findByRollNumber_ShouldReturnEmpty_WhenNotExists() {

        when(studentRepository.findByRollNumber(999)).thenReturn(Optional.empty());

        Optional<Student> foundStudent = studentRepository.findByRollNumber(999);

        assertFalse(foundStudent.isPresent());
        verify(studentRepository, times(1)).findByRollNumber(999);
    }

    @Test
    void existsById_ShouldReturnTrue_WhenStudentExists() {
        when(studentRepository.existsById("1")).thenReturn(true);

        boolean exists = studentRepository.existsById("1");

        assertTrue(exists);
        verify(studentRepository, times(1)).existsById("1");
    }

    @Test
    void existsById_ShouldReturnFalse_WhenStudentDoesNotExist() {
        when(studentRepository.existsById("999")).thenReturn(false);

        boolean exists = studentRepository.existsById("999");

        assertFalse(exists);
        verify(studentRepository, times(1)).existsById("999");
    }

    @Test
    void deleteById_ShouldInvokeDelete_WhenCalled() {

        doNothing().when(studentRepository).deleteById("1");

        studentRepository.deleteById("1");

        verify(studentRepository, times(1)).deleteById("1");
    }
}