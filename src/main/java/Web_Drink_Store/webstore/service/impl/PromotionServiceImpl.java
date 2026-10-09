package Web_Drink_Store.webstore.service.impl;

import Web_Drink_Store.webstore.dto.promotion.PromotionRequest;
import Web_Drink_Store.webstore.dto.promotion.PromotionResponse;
import Web_Drink_Store.webstore.entity.Promotion;
import Web_Drink_Store.webstore.enums.PromotionStatus;
import Web_Drink_Store.webstore.exception.BadRequestException;
import Web_Drink_Store.webstore.exception.ResourceNotFoundException;
import Web_Drink_Store.webstore.repository.PromotionRepository;
import Web_Drink_Store.webstore.service.PromotionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromotionServiceImpl implements PromotionService {

    private final PromotionRepository promotionRepository;

    public PromotionServiceImpl(
            PromotionRepository promotionRepository
    ) {
        this.promotionRepository = promotionRepository;
    }

    @Override
    public List<PromotionResponse> getAll() {
        return promotionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public PromotionResponse getById(Long id) {

        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Không tìm thấy khuyến mãi"
                        )
                );

        return toResponse(promotion);
    }

    @Override
    public PromotionResponse create(
            PromotionRequest request
    ) {

        if (request.getCode() == null
                || request.getCode().isBlank()) {

            throw new BadRequestException(
                    "Mã khuyến mãi không được để trống"
            );
        }

        if (promotionRepository.existsByCode(
                request.getCode())) {

            throw new BadRequestException(
                    "Mã khuyến mãi đã tồn tại"
            );
        }

        validate(request);

        Promotion promotion = new Promotion();

        promotion.setCode(request.getCode());
        promotion.setName(request.getName());
        promotion.setDiscountType(
                request.getDiscountType()
        );
        promotion.setDiscountValue(
                request.getDiscountValue()
        );
        promotion.setMinOrderValue(
                request.getMinOrderValue()
        );
        promotion.setMaxDiscountValue(
                request.getMaxDiscountValue()
        );
        promotion.setStartAt(request.getStartAt());
        promotion.setEndAt(request.getEndAt());
        promotion.setStatus(PromotionStatus.ACTIVE);

        promotionRepository.save(promotion);

        return toResponse(promotion);
    }

    @Override
    public PromotionResponse update(
            Long id,
            PromotionRequest request
    ) {

        Promotion promotion =
                promotionRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy khuyến mãi"
                                )
                        );

        if (request.getCode() == null
                || request.getCode().isBlank()) {

            throw new BadRequestException(
                    "Mã khuyến mãi không được để trống"
            );
        }

        promotionRepository.findByCode(
                        request.getCode()
                )
                .ifPresent(existing -> {

                    if (!existing.getId().equals(id)) {

                        throw new BadRequestException(
                                "Mã khuyến mãi đã tồn tại"
                        );
                    }
                });

        validate(request);

        promotion.setCode(request.getCode());
        promotion.setName(request.getName());
        promotion.setDiscountType(
                request.getDiscountType()
        );
        promotion.setDiscountValue(
                request.getDiscountValue()
        );
        promotion.setMinOrderValue(
                request.getMinOrderValue()
        );
        promotion.setMaxDiscountValue(
                request.getMaxDiscountValue()
        );
        promotion.setStartAt(request.getStartAt());
        promotion.setEndAt(request.getEndAt());

        promotionRepository.save(promotion);

        return toResponse(promotion);
    }

    @Override
    public void deactivate(Long id) {

        Promotion promotion =
                promotionRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Không tìm thấy khuyến mãi"
                                )
                        );

        if (promotion.getStatus() == PromotionStatus.ACTIVE) {
            promotion.setStatus(PromotionStatus.INACTIVE);
        } else {
            promotion.setStatus(PromotionStatus.ACTIVE);
        }

        promotionRepository.save(promotion);
    }

    private void validate(
            PromotionRequest request
    ) {

        if (request.getName() == null
                || request.getName().isBlank()) {

            throw new BadRequestException(
                    "Tên khuyến mãi không được để trống"
            );
        }

        if (request.getDiscountType() == null) {

            throw new BadRequestException(
                    "Loại giảm giá không được để trống"
            );
        }

        if (request.getDiscountValue() == null
                || request.getDiscountValue()
                .signum() <= 0) {

            throw new BadRequestException(
                    "Giá trị giảm phải lớn hơn 0"
            );
        }

        if (request.getMinOrderValue() != null
                && request.getMinOrderValue()
                .signum() < 0) {

            throw new BadRequestException(
                    "Giá trị đơn hàng tối thiểu không được âm"
            );
        }

        if (request.getMaxDiscountValue() != null
                && request.getMaxDiscountValue()
                .signum() < 0) {

            throw new BadRequestException(
                    "Mức giảm tối đa không được âm"
            );
        }

        if (request.getStartAt() != null
                && request.getEndAt() != null
                && request.getEndAt()
                .isBefore(request.getStartAt())) {

            throw new BadRequestException(
                    "Thời gian kết thúc phải sau thời gian bắt đầu"
            );
        }
    }

    private PromotionResponse toResponse(
            Promotion promotion
    ) {

        return new PromotionResponse(
                promotion.getId(),
                promotion.getCode(),
                promotion.getName(),
                promotion.getDiscountType(),
                promotion.getDiscountValue(),
                promotion.getMinOrderValue(),
                promotion.getMaxDiscountValue(),
                promotion.getStartAt(),
                promotion.getEndAt(),
                promotion.getStatus()
        );
    }
}