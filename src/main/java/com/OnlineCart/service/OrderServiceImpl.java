package com.OnlineCart.service;

import com.OnlineCart.Utils.CommonUtil;
import com.OnlineCart.Utils.OrderStatus;
import com.OnlineCart.model.Cart;
import com.OnlineCart.model.OrderAddress;
import com.OnlineCart.model.OrderRequset;
import com.OnlineCart.model.ProductOrder;
import com.OnlineCart.repository.CartRepository;
import com.OnlineCart.repository.ProductOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService{

    @Autowired
    private ProductOrderRepository productOrderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CommonUtil commonUtil;

    @Override
    public void saveOrder(Integer userId, OrderRequset orderRequset) throws Exception {

       List<Cart> carts = cartRepository.findByUserId(userId);

       for(Cart cart:carts)
       {
           ProductOrder order = new ProductOrder();
           order.setOrderId(UUID.randomUUID().toString());
           order.setOrderDate(LocalDate.now());
           order.setProduct(cart.getProduct());
           order.setPrice(cart.getProduct().getDiscountPrice());
           order.setQuantity(cart.getQuantity());
           order.setUser(cart.getUser());

           order.setStatus(OrderStatus.IN_PROGRESS.getName());
           order.setPaymentType(orderRequset.getPaymentType());

           OrderAddress address = new OrderAddress();
           address.setFirstName(orderRequset.getFirstName());
           address.setLastName(orderRequset.getLastName());
           address.setEmail(orderRequset.getEmail());
           address.setMobileNo(orderRequset.getMobileNo());
           address.setAddress(orderRequset.getAddress());
           address.setCity(orderRequset.getCity());
           address.setState(orderRequset.getState());
           address.setPincode(orderRequset.getPincode());

           order.setOrderAddress(address);

           ProductOrder saveOrder = productOrderRepository.save(order);
           commonUtil.sendMailForProductOrder(saveOrder, "success");
       }


    }

    @Override
    public List<ProductOrder> getOrdersByUser(Integer userId) {
        List<ProductOrder> orders = productOrderRepository.findByUserId(userId);
        return orders;
    }

    @Override
    public ProductOrder updateOrderStatus(Integer id, String status) {
        Optional<ProductOrder> findById = productOrderRepository.findById(id);
        if (findById.isPresent()) {
            ProductOrder productOrder = findById.get();
            productOrder.setStatus(status);
            ProductOrder updateOrder = productOrderRepository.save(productOrder);
            return updateOrder;
        }
        return null;
    }

    @Override
    public List<ProductOrder> getAllOrders() {
       return productOrderRepository.findAll();

    }

    @Override
    public ProductOrder getOrdersByOrderId(String orderId) {
        return productOrderRepository.findByOrderId(orderId);
    }
}
