package org.example.dto.email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.vo.NewSubmissionVO;

import java.util.List;

/**
 * @author chenxuanrao06@gmail.com
 * @Description:
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotifyNewSubmissionDTO {
    private String email;
    private String username;
    private List<NewSubmissionVO> newSubmissionVOs;
}
