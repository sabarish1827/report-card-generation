package com.evaluate.report_card_system.controller;

import com.evaluate.report_card_system.request.UpdateMarkRequest;
import com.evaluate.report_card_system.model.Student;
import com.evaluate.report_card_system.repository.StudentRepository;
import com.evaluate.report_card_system.service.ReportCardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ReportCardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReportCardService reportCardService;

    @Mock
    private StudentRepository studentRepository;

    private ObjectMapper objectMapper;

    private Student sampleStudent;

    @BeforeEach
    void setUp() {

        ReportCardController controller = new ReportCardController(reportCardService, studentRepository);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        objectMapper = new ObjectMapper();

        sampleStudent = new Student();
        sampleStudent.setId("1");
        sampleStudent.setRollNumber(101);
        sampleStudent.setName("John Doe");
    }

    @Test
    void generateReportCard_ShouldReturnFinalScore_WhenValidRequest() throws Exception {

        when(studentRepository.save(any(Student.class))).thenReturn(sampleStudent);
        when(reportCardService.calculateFinalScore(any(Student.class))).thenReturn(81.67);

        String requestJson = objectMapper.writeValueAsString(sampleStudent);

        mockMvc.perform(post("/api/reportcard/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("81.67"));

        verify(studentRepository, times(2)).save(sampleStudent);
        verify(reportCardService, times(1)).calculateFinalScore(sampleStudent);
    }

    @Test
    void generateReportCard_ShouldReturnBadRequest_WhenInvalidStudent() throws Exception {

        Student invalidStudent = new Student();
        when(reportCardService.calculateFinalScore(any(Student.class)))
                .thenThrow(new IllegalArgumentException("Student must have at least one term"));

        String requestJson = objectMapper.writeValueAsString(invalidStudent);

        mockMvc.perform(post("/api/reportcard/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Student must have at least one term"));

        verify(studentRepository, times(1)).save(invalidStudent);
        verify(reportCardService, times(1)).calculateFinalScore(invalidStudent);
    }

    @Test
    void getStudentByRollNo_ShouldReturnStudent_WhenFound() throws Exception {

        when(studentRepository.findByRollNumber(101)).thenReturn(Optional.of(sampleStudent));

        mockMvc.perform(get("/api/reportcard/roll/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rollNumber").value(101))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(studentRepository, times(1)).findByRollNumber(101);
    }

    @Test
    void getStudentByRollNo_ShouldReturnNotFound_WhenStudentDoesNotExist() throws Exception {

        when(studentRepository.findByRollNumber(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/reportcard/roll/999"))
                .andExpect(status().isNotFound());

        verify(studentRepository, times(1)).findByRollNumber(999);
    }

    @Test
    void updateExamMarks_ShouldUpdateAndReturnStudent_WhenValidRequest() throws Exception {

        UpdateMarkRequest request = new UpdateMarkRequest();
        request.setTermName("Term 1");
        request.setExamName("Exam 1");
        request.setSubjectMarks(new HashMap<>() {{
            put("Physics", 90.0);
        }});

        when(studentRepository.findByRollNumber(101)).thenReturn(Optional.of(sampleStudent));
        doNothing().when(reportCardService).updateExamMarks(any(Student.class), any(UpdateMarkRequest.class));
        when(studentRepository.save(sampleStudent)).thenReturn(sampleStudent);

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/reportcard/roll/101/marks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rollNumber").value(101));

        verify(studentRepository, times(1)).findByRollNumber(101);
        verify(reportCardService, times(1)).updateExamMarks(sampleStudent, request);
        verify(studentRepository, times(1)).save(sampleStudent);
    }

    @Test
    void updateExamMarks_ShouldReturnNotFound_WhenStudentDoesNotExist() throws Exception {

        UpdateMarkRequest request = new UpdateMarkRequest();
        request.setTermName("Term 1");
        request.setExamName("Exam 1");
        request.setSubjectMarks(new HashMap<>() {{
            put("Physics", 90.0);
        }});

        when(studentRepository.findByRollNumber(999)).thenReturn(Optional.empty());

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/reportcard/roll/999/marks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound());

        verify(studentRepository, times(1)).findByRollNumber(999);
        verify(reportCardService, never()).updateExamMarks(any(), any());
    }

    @Test
    void updateExamMarks_ShouldReturnBadRequest_WhenInvalidRequest() throws Exception {

        UpdateMarkRequest request = new UpdateMarkRequest();
        request.setTermName("Term 1");
        request.setExamName("Exam 4");
        request.setSubjectMarks(new HashMap<>() {{
            put("Physics", 90.0);
        }});

        when(studentRepository.findByRollNumber(101)).thenReturn(Optional.of(sampleStudent));
        doThrow(new IllegalArgumentException("Exam Exam 4 not found in term Term 1"))
                .when(reportCardService).updateExamMarks(any(Student.class), any(UpdateMarkRequest.class));

        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/reportcard/roll/101/marks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Exam Exam 4 not found in term Term 1"));

        verify(studentRepository, times(1)).findByRollNumber(101);
        verify(reportCardService, times(1)).updateExamMarks(sampleStudent, request);
    }

    @Test
    void deleteStudent_ShouldReturnNoContent_WhenStudentExists() throws Exception {

        when(studentRepository.existsById("1")).thenReturn(true);
        doNothing().when(studentRepository).deleteById("1");

        mockMvc.perform(delete("/api/reportcard/1"))
                .andExpect(status().isNoContent());

        verify(studentRepository, times(1)).existsById("1");
        verify(studentRepository, times(1)).deleteById("1");
    }

    @Test
    void deleteStudent_ShouldReturnNotFound_WhenStudentDoesNotExist() throws Exception {

        when(studentRepository.existsById("999")).thenReturn(false);

        mockMvc.perform(delete("/api/reportcard/999"))
                .andExpect(status().isNotFound());

        verify(studentRepository, times(1)).existsById("999");
        verify(studentRepository, never()).deleteById("999");
    }
}