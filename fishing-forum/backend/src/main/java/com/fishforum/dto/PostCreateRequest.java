package com.fishforum.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PostCreateRequest {
    @NotBlank(message = "标题不能为空")
    @Size(max = 100, message = "标题不能超过100个字符")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @NotNull(message = "板块不能为空")
    private Long sectionId;

    private Long tagId;
    private String postType = "NORMAL";

    @Valid
    private CatchRecordRequest catchRecord;

    @Valid
    private GearReviewRequest gearReview;

    @Data
    public static class CatchRecordRequest {
        @NotBlank(message = "鱼种不能为空")
        @Size(max = 100, message = "鱼种不能超过100个字符")
        private String fishSpecies;
        @DecimalMin(value = "0.0", message = "重量不能为负数")
        private Double weight;
        @DecimalMin(value = "0.0", message = "长度不能为负数")
        private Double length;
        private String bait;
        private String spotName;
        private String weather;
        private String photoUrl;
        private LocalDate fishingDate;
    }

    @Data
    public static class GearReviewRequest {
        @NotBlank(message = "品牌不能为空")
        private String brand;
        @NotBlank(message = "型号不能为空")
        private String model;
        @NotBlank(message = "装备分类不能为空")
        private String gearCategory;
        @DecimalMin(value = "0.0", message = "价格不能为负数")
        private Double price;
        @NotNull(message = "评分不能为空")
        @Min(value = 1, message = "评分最低为1")
        @Max(value = 5, message = "评分最高为5")
        private Integer rating;
        private String pros;
        private String cons;
        private String photoUrl;
    }
}
