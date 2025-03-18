package com.evaluate.report_card_system.controller;

import com.evaluate.report_card_system.model.Exam;
import com.evaluate.report_card_system.model.Student;
import com.evaluate.report_card_system.model.Term;
import com.evaluate.report_card_system.request.UpdateMarkRequest;
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

    private ObjectMapper objectMapper;

    private Student sampleStudent;

    @BeforeEach
    void setUp() {
        ReportCardController controller = new ReportCardController(reportCardService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        objectMapper = new ObjectMapper();

        sampleStudent = new Student();
        sampleStudent.setId("1");
        sampleStudent.setRollNumber(101);
        sampleStudent.setName("John Doe");

        Exam exam1 = new Exam();
        exam1.setExamName("Exam 1");
        exam1.setSubjectMarks(new HashMap<>() {{
            put("Physics", 78.0);
            put("Chemistry", 72.0);
            put("Biology", 80.0);
        }});

        Term term1 = new Term();
        term1.setTermName("Term 1");
        term1.setExams(List.of(exam1));
        term1.setTermScore(76.8);

        sampleStudent.setTerms(List.of(term1));
    }

    @Test
    void generateReportCard_ShouldReturnFinalScore_WhenValid() throws Exception {
        when(reportCardService.generateReportCard(any(Student.class))).thenReturn(sampleStudent);
        String requestJson = objectMapper.writeValueAsString(sampleStudent);

        mockMvc.perform(post("/api/reportcard/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().string("76.8"));
    }

    @Test
    void generateReportCard_ShouldReturnBadRequest_WhenNegativeRollNumber() throws Exception {
        Student invalidStudent = new Student();
        invalidStudent.setRollNumber(-1);
        invalidStudent.setName("John Doe");
        when(reportCardService.generateReportCard(invalidStudent))
                .thenThrow(new IllegalArgumentException("Roll number must be a positive integer"));
        String requestJson = objectMapper.writeValueAsString(invalidStudent);

        mockMvc.perform(post("/api/reportcard/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Roll number must be a positive integer"));
    }

    @Test
    void generateReportCard_ShouldReturnBadRequest_WhenTermNameNull() throws Exception {
        Student invalidStudent = new Student();
        invalidStudent.setRollNumber(101);
        invalidStudent.setName("John Doe");
        Term term = new Term();
        term.setTermName(null);
        term.setExams(List.of(new Exam()));
        invalidStudent.setTerms(List.of(term));
        when(reportCardService.generateReportCard(invalidStudent))
                .thenThrow(new IllegalArgumentException("Term name is required"));
        String requestJson = objectMapper.writeValueAsString(invalidStudent);

        mockMvc.perform(post("/api/reportcard/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Term name is required"));
    }

    @Test
    void updateExamMarks_ShouldReturnStudent_WhenValid() throws Exception {
        UpdateMarkRequest request = new UpdateMarkRequest();
        request.setTermName("Term 1");
        request.setExamName("Exam 1");
        request.setSubjectMarks(new HashMap<>() {{
            put("Physics", 90.0);
        }});

        when(reportCardService.updateExamMarks(101, request)).thenReturn(sampleStudent);
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/reportcard/roll/101/marks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());
    }

    @Test
    void updateExamMarks_ShouldReturnBadRequest_WhenNegativeRollNumber() throws Exception {
        UpdateMarkRequest request = new UpdateMarkRequest();
        request.setTermName("Term 1");
        request.setExamName("Exam 1");
        when(reportCardService.updateExamMarks(-1, request))
                .thenThrow(new IllegalArgumentException("Roll number must be a positive integer"));
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/reportcard/roll/-1/marks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Roll number must be a positive integer"));
    }

    @Test
    void updateExamMarks_ShouldReturnBadRequest_WhenTermNameNull() throws Exception {
        UpdateMarkRequest request = new UpdateMarkRequest();
        request.setTermName(null);
        request.setExamName("Exam 1");
        request.setSubjectMarks(new HashMap<>() {{
            put("Physics", 90.0);
        }});
        when(reportCardService.updateExamMarks(101, request))
                .thenThrow(new IllegalArgumentException("Term name is required"));
        String requestJson = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/api/reportcard/roll/101/marks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Term name is required"));
    }

    @Test
    void deleteStudent_ShouldReturnNoContent_WhenValid() throws Exception {
        doNothing().when(reportCardService).deleteStudent("1");

        mockMvc.perform(delete("/api/reportcard/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteStudent_ShouldReturnBadRequest_WhenIdNull() throws Exception {
        doThrow(new IllegalArgumentException("ID is required")).when(reportCardService).deleteStudent("null");

        mockMvc.perform(delete("/api/reportcard/null"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("ID is required"));
    }

    @Test
    void getStudentByRollNo_ShouldReturnStudent_WhenFound() throws Exception {
        when(reportCardService.getStudentByRollNumber(101)).thenReturn(Optional.of(sampleStudent));

        mockMvc.perform(get("/api/reportcard/roll/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rollNumber").value(101));
    }

    @Test
    void getStudentByRollNo_ShouldReturnNotFound_WhenNotExists() throws Exception {
        when(reportCardService.getStudentByRollNumber(999)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/reportcard/roll/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getStudentByRollNo_ShouldReturnBadRequest_WhenRollNumberNegative() throws Exception {
        when(reportCardService.getStudentByRollNumber(-1))
                .thenThrow(new IllegalArgumentException("Roll number must be a positive integer"));

        mockMvc.perform(get("/api/reportcard/roll/-1"))
                .andExpect(status().isBadRequest());
    }
}