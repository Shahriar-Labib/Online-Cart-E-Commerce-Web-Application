package com.OnlineCart.controller;

import com.OnlineCart.Utils.CommonUtil;
import com.OnlineCart.Utils.OrderStatus;
import com.OnlineCart.model.*;
import com.OnlineCart.service.CartService;
import com.OnlineCart.service.CategoryService;
import com.OnlineCart.service.OrderService;
import com.OnlineCart.service.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CommonUtil commonUtil;

    @ModelAttribute
    public void getUserDetails(Principal principal, Model model)
    {
        if(principal != null)
        {
            String email = principal.getName();
            UserDatas userDatas = userDetailsService.getUserByEmail(email);

            model.addAttribute("user",userDatas);

            Integer countCart = cartService.getCountCart(userDatas.getId());
            model.addAttribute("countCart",countCart);
        }
        List<Category> getAllActiveCategories = categoryService.getAllActiveCategory();
        model.addAttribute("categorys",getAllActiveCategories);

    }

    @GetMapping("/")
    public String home()
    {
        return "user_home";

    }

    @GetMapping("/addCart")
    public String addToCart(@RequestParam Integer pid, @RequestParam Integer uid, RedirectAttributes session) {
        Cart saveCart = cartService.saveCart(pid, uid);

        if (ObjectUtils.isEmpty(saveCart)) {
            session.addFlashAttribute("errorMsg", "Product add to cart failed");
        }else {
            session.addFlashAttribute("successMsg", "Product added to cart");
        }
        return "redirect:/product/" + pid;
    }

    @GetMapping("/cart")
    public String loadCartPage(Principal p,Model model)
    {
        UserDatas user = getLoggedInUserDetails(p);
       List<Cart> carts = cartService.getCartsByUser(user.getId());
       model.addAttribute("carts",carts);
       if(carts.size() > 0)
       {
           model.addAttribute("totalOrderPrice",carts.get(carts.size()-1).getTotalOrderAmountPrice());
       }

       return "cart";
    }

    private UserDatas getLoggedInUserDetails(Principal p) {

        String email = p.getName();
        UserDatas userDatas = userDetailsService.getUserByEmail(email);
        return userDatas;
    }

    @GetMapping("/cartQuantityUpdate")
    public String updateCartQuantity(@RequestParam String sy,@RequestParam Integer cid)
    {
        cartService.updateQuantity(sy,cid);
        return "redirect:/user/cart";
    }

    @GetMapping("/orders")
    public String orderPage(Principal p,Model model)
    {
        UserDatas user = getLoggedInUserDetails(p);
        List<Cart> carts = cartService.getCartsByUser(user.getId());
        model.addAttribute("carts",carts);
        if(carts.size() > 0)
        {
            Double totalOrderPrice = carts.get(carts.size()-1).getTotalOrderAmountPrice()+250+100;
            Double orderPrice = carts.get(carts.size()-1).getTotalOrderAmountPrice();
            model.addAttribute("totalOrderPrice",totalOrderPrice);
            model.addAttribute("orderPrice",orderPrice);
        }
        return "order";
    }

    @PostMapping("/save-order")
    public String saveOrder(@ModelAttribute OrderRequset requset,Principal p) throws Exception
    {
       // System.out.println(requset);

        UserDatas user = getLoggedInUserDetails(p);
        orderService.saveOrder(user.getId(), requset);
        return "redirect:/user/success";
    }

    @GetMapping("/success")
    public String loadSuccess()
    {
        return "success";
    }

    @GetMapping("/user-orders")
    public String myOrder(Model model,Principal p)
    {
        UserDatas loginUser = getLoggedInUserDetails(p);
        List<ProductOrder> orders = orderService.getOrdersByUser(loginUser.getId());
        model.addAttribute("orders", orders);
        return "my_order";
    }

    @GetMapping("/update-status")
    public String updateOrderStatus(@RequestParam Integer id,@RequestParam Integer st,RedirectAttributes session)
    {

        OrderStatus[] values = OrderStatus.values();
        String status = null;

        for (OrderStatus orderSt : values) {
            if (orderSt.getId().equals(st)) {
                status = orderSt.getName();
            }
        }

        ProductOrder updateOrder = orderService.updateOrderStatus(id, status);

        try {
            commonUtil.sendMailForProductOrder(updateOrder, status);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!ObjectUtils.isEmpty(updateOrder)) {
            session.addFlashAttribute("successMsg", "Status Updated");
        } else {
            session.addFlashAttribute("", "status not updated");
        }
        return "redirect:/user/user-orders";
    }

    @GetMapping("/profile")
    public String profile()
    {
        return "profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(@ModelAttribute UserDatas user,
                                @RequestParam MultipartFile img,
                                RedirectAttributes session)
    {
       UserDatas updateUserProfile = userDetailsService.updateUserProfile(user,img);
        if(ObjectUtils.isEmpty(updateUserProfile))
        {
            session.addFlashAttribute("errorMsg","Profile Not Updated");
        }
        else{
            session.addFlashAttribute("successMsg","Profile Updated Successfully");
        }

        return "redirect:/user/profile";
    }


}
