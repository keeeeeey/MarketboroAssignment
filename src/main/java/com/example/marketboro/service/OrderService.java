package com.example.marketboro.service;

import com.example.marketboro.dto.request.CommonDto.IdDto;
import com.example.marketboro.dto.request.CommonDto.OrderDto;
import com.example.marketboro.dto.request.OrderRequestDto.CancelOrderDto;
import com.example.marketboro.dto.request.OrderRequestDto.CreateOrderDto;
import com.example.marketboro.dto.response.OrderProductResponseDto;
import com.example.marketboro.entity.*;
import com.example.marketboro.exception.ErrorCode;
import com.example.marketboro.exception.ErrorCustomException;
import com.example.marketboro.repository.cart.CartProductRepository;
import com.example.marketboro.repository.order.OrderProductRepository;
import com.example.marketboro.repository.order.OrderRepository;
import com.example.marketboro.repository.ProductRepository;
import com.example.marketboro.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartProductRepository cartProductRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;

    @Transactional
    public List<OrderProductResponseDto> createOrder(Long userId, CreateOrderDto requestDto, HttpServletRequest request) {
        HttpSession httpSession = request.getSession();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ErrorCustomException(ErrorCode.NO_USER_ERROR));
        Order order = Order.builder()
                .user(user)
                .build();
        Order saveOrder = orderRepository.save(order);
        List<OrderDto> orderDtoList = requestDto.getOrderList();
        List<OrderProductResponseDto> responseDtoList = setOrderProductList(saveOrder, orderDtoList);
        httpSession.setAttribute("orderId", order.getId());
        return responseDtoList;
    }

    @Transactional
    public List<OrderProductResponseDto> cancelOrder(CancelOrderDto requestDto) {
        List<IdDto> cancelOrderList = requestDto.getCancelOrderList();
        List<OrderProductResponseDto> responseDtoList = setCancelOrderProductList(cancelOrderList);
        return responseDtoList;
    }

    @Transactional
    public OrderProductResponseDto finishDelivery(Long orderProductId) {
        OrderProduct orderProduct = orderProductRepository.findById(orderProductId)
                .orElseThrow(() -> new ErrorCustomException(ErrorCode.NO_ORDERED_ERROR));
        orderProduct.changeOrderStatus(OrderStatus.배송완료);
        log.info(orderProduct.getId() + "번 주문 배송 완료");
        return OrderProductResponseDto.builder()
                .orderProduct(orderProduct)
                .build();
    }

    @Transactional(readOnly = true)
    public List<OrderProductResponseDto> getAllOrder(Long userId) {
        List<Order> orderList = orderRepository.findAllByUserId(userId);
        List<OrderProductResponseDto> responseDto = new ArrayList<>();
        orderList.forEach((order) -> {
            List<OrderProductResponseDto> findOrderProductList = orderProductRepository.findOrderProductByOrderId(order.getId());
            findOrderProductList.forEach((findOrderProduct) -> {
                responseDto.add(findOrderProduct);
            });
        });
        return responseDto;
    }

    private List<OrderProductResponseDto> setOrderProductList(Order saveOrder, List<OrderDto> orderDtoList) {
        List<OrderProductResponseDto> responseDtoList = new ArrayList<>();
        orderDtoList.forEach((orderDto) -> {
            Product product = productRepository.findById(orderDto.getProductId())
                    .orElseThrow(() -> new ErrorCustomException(ErrorCode.NO_EXISTENCE_ERROR));

            if (product.getLeftProduct() < orderDto.getProductCount()) {
                throw new ErrorCustomException(ErrorCode.SHORTAGE_PRODUCT_ERROR);
            }

            OrderProduct orderProduct = OrderProduct.builder()
                    .order(saveOrder)
                    .product(product)
                    .productCount(orderDto.getProductCount())
                    .orderStatus(OrderStatus.주문접수)
                    .build();

            OrderProduct saveOrderProduct = orderProductRepository.save(orderProduct);
            product.minusLeftProduct(orderDto.getProductCount());

            cartProductRepository.deleteById(orderDto.getCartProductId());

            OrderProductResponseDto responseDto = OrderProductResponseDto.builder()
                    .orderProduct(saveOrderProduct)
                    .build();

            responseDtoList.add(responseDto);
            log.info(saveOrderProduct.getId() + "번 상품 주문 접수");
        });
        return responseDtoList;
    }

    private List<OrderProductResponseDto> setCancelOrderProductList(List<IdDto> cancelOrderList) {
        List<OrderProductResponseDto> responseDtoList = new ArrayList<>();
        cancelOrderList.forEach((cancelOrder) -> {
            OrderProduct orderProduct = orderProductRepository.findById(cancelOrder.getId())
                    .orElseThrow(() -> new ErrorCustomException(ErrorCode.ALREADY_CANCELORDER_ERROR));

            if (orderProduct.getOrderStatus().equals(OrderStatus.배송완료)) {
                throw new ErrorCustomException(ErrorCode.ALREADY_DELIVERED_ERROR);
            } else if (orderProduct.getOrderStatus().equals(OrderStatus.주문취소)) {
                throw new ErrorCustomException(ErrorCode.ALREADY_CANCELORDER_ERROR);
            }

            orderProduct.changeOrderStatus(OrderStatus.주문취소);
            orderProduct.getProduct().plusLeftProduct(orderProduct.getProductCount());

            OrderProductResponseDto responseDto = OrderProductResponseDto.builder()
                    .orderProduct(orderProduct)
                    .build();

            responseDtoList.add(responseDto);
            log.info(orderProduct.getId() + "번 주문 취소");
        });
        return responseDtoList;
    }

}
