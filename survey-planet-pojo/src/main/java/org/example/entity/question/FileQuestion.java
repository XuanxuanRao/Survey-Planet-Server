package org.example.entity.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.entity.response.ResponseItem;
import org.example.vo.QuestionAnalyseVO;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileQuestion extends Question {
    /**
     * Maximum file size allow to upload(MB)
     */
    private Integer maxFileSize;
}
