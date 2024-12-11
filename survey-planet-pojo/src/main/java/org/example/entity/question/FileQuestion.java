package org.example.entity.question;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.example.dto.QuestionDTO;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileQuestion extends Question {
    /**
     * Maximum file size allow to upload(MB)
     */
    private Integer maxFileSize;

    @Override
    public QuestionDTO toQuestionDTO() {
        var res = super.toQuestionDTO();
        res.setMaxFileSize(maxFileSize);
        return res;
    }
}
