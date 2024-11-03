package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.entity.response.ResponseItem;

import java.util.List;

/**
 * @author chenxuanrao06@gmail.com
 * @ClassName ResponseDTO
 * @description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDTO {
    private Long sid;
    private List<ResponseItem> items;
}
