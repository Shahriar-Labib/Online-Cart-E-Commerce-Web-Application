package com.OnlineCart.controller;

import com.OnlineCart.Utils.CommonUtil;
import com.OnlineCart.Utils.OrderStatus;
import com.OnlineCart.model.Category;
import com.OnlineCart.model.Product;
import com.OnlineCart.model.ProductOrder;
import com.OnlineCart.model.UserDatas;
import com.OnlineCart.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserDetailsService userDetailsService;

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
    public String index()
    {
        return "adminindex";
    }

    @GetMapping("/addproduct")
    public String addproduct(Model model)
    {
        List<Category> categories = categoryService.getAllCategory();
        model.addAttribute("categories",categories);
        return "add_product";
    }

    @GetMapping("/category")
    public String category(Model model)
    {
        model.addAttribute("categorys",categoryService.getAllCategory());
        return "categorey";
    }

    @PostMapping("/saveCategory")
    public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes)
    throws IOException {


        String imageName = "default.jpg";

        if (!file.isEmpty()) {
            // You can save the file to a directory or process it here
            imageName = file.getOriginalFilename();  // For example, get the file's name
            // Optionally save the file to your server here
        }

        category.setImageName(imageName);


        if (categoryService.existsCategory(category.getName())) {
            redirectAttributes.addFlashAttribute("errorMsg", "Category name already exists");
        } else {
            Category check = categoryService.saveCategory(category);
            if (check == null) {
                redirectAttributes.addFlashAttribute("errorMsg", "Not saved, internal server error");
            } else {
                File saveFile =  new ClassPathResource("static/img").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"category_img"+File.separator+file.getOriginalFilename());

                System.out.println(path);
                Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                redirectAttributes.addFlashAttribute("successMsg", "Saved successfully");
            }
        }
        return "redirect:/admin/category";
    }

    @GetMapping("/deleteCategory/{id}")
    public String deleteCategory(@PathVariable int id,RedirectAttributes session)
    {
        Boolean deleteCategory = categoryService.deleteCategory(id);

        if(deleteCategory)
        {
            session.addFlashAttribute("successMsg","category delete successfully");
        }
        else{
            session.addFlashAttribute("errorMsg","something went wrong");
        }

        return "redirect:/admin/category";
    }

    @GetMapping("/loadEditCategory/{id}")
    public String loadEditCategory(@PathVariable int id,Model model)
    {
        model.addAttribute("category",categoryService.getCategoryById(id));
        return "edit_category";
    }

    @PostMapping("/updateCategory")
    public String updateCategory(@ModelAttribute Category category,
                                 @RequestParam("file") MultipartFile file,
                                 RedirectAttributes session) throws IOException
    {
        Category oldCategory = categoryService.getCategoryById(category.getId());

        String imageName = file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();

        if(!ObjectUtils.isEmpty(category))
        {
            if(!file.isEmpty())
            {
                File saveFile =  new ClassPathResource("static/img").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"category_img"+File.separator+file.getOriginalFilename());

                System.out.println(path);
                Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
                session.addFlashAttribute("successMsg", "Saved successfully");
            }


            oldCategory.setName(category.getName());
            oldCategory.setIsActive(category.getIsActive());
            oldCategory.setImageName(imageName);
        }

        Category updateCategory = categoryService.saveCategory(oldCategory);

        if(!ObjectUtils.isEmpty(updateCategory))
        {
            session.addFlashAttribute("successMsg","Updated Successfully");
        }
        else{
            session.addFlashAttribute("errorMsg","Something went wrong");

        }

        return "redirect:/admin/loadEditCategory/" + category.getId();
    }

    @PostMapping("/saveProduct")
    public String saveProduct(@ModelAttribute Product product, RedirectAttributes session,@RequestParam("file") MultipartFile image) throws IOException
    {
        String imageName = image.isEmpty() ? "default.jpg" : image.getOriginalFilename();
        product.setImage(imageName);
        product.setDiscount(0);
        product.setDiscountPrice(product.getPrice());

        Product saveProduct = productService.saveProduct(product);

        if(!ObjectUtils.isEmpty(saveProduct))
        {
            File saveFile =  new ClassPathResource("static/img").getFile();

            Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"product_img"+File.separator+image.getOriginalFilename());

            System.out.println(path);
            Files.copy(image.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);


            session.addFlashAttribute("successMsg","Product saved successfully");
        }
        else{
            session.addFlashAttribute("errorMsg","Something went wrong");
        }
        return "redirect:/admin/addproduct";
    }


    @GetMapping("/products")
    public String viewProduct(Model model)
    {
        model.addAttribute("products",productService.getAllProduct());
        return "products_page";
    }

    @GetMapping("/deleteProduct/{id}")
    public String deleteProduct(@PathVariable int id,RedirectAttributes session)
    {
        Boolean deleteProduct = productService.deleteProduct(id);

        if(deleteProduct)
        {
            session.addFlashAttribute("successMsg","Product delete successfully");
        }
        else{
            session.addFlashAttribute("errorMsg","Something is wrong in the server");
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/editProduct/{id}")
    public String editProduct(@PathVariable int id,Model model)
    {
        model.addAttribute("product",productService.getProductById(id));
        model.addAttribute("categories",categoryService.getAllCategory());
        return "edit_product";
    }
@PostMapping("/updateProduct")
public String updateProduct(Model model,
                            @RequestParam("file") MultipartFile image,
                            @ModelAttribute Product product,
                            RedirectAttributes session) throws IOException {

        Double check  = product.getDiscountPrice();
if(product.getDiscount() < 0 || product.getDiscount() > 100)
{
    session.addFlashAttribute("errorMsg", "Invalid discount price");
}
else{
    Product updated = productService.updateProduct(product, image);

    if (!ObjectUtils.isEmpty(updated)) {
        // service handles file saving now — only show feedback here
        session.addFlashAttribute("successMsg", "Product updated successfully");
    } else {
        session.addFlashAttribute("errorMsg", "Something is wrong in the server");
    }
}




    return "redirect:/admin/editProduct/" + product.getId();
}

@GetMapping("/users")
public String getAllUsers(Model model)
{
   List<UserDatas> users = userDetailsService.getUsers("ROLE_USER");
    model.addAttribute("users",users);
   return "user_home";
}

@GetMapping("/updatestatus")
public String updateAccountStatus(@RequestParam Boolean status,
                                  @RequestParam Integer id,
                                  RedirectAttributes session)
{
  Boolean f = userDetailsService.updateAccountStatus(id,status);

   if(f)
   {
       session.addFlashAttribute("successMsg","Account status Update");
   }
   else{
       session.addFlashAttribute("errorMsg","Something is wrong");
   }

    return "redirect:/admin/users";
}

    @GetMapping("/orders")
    public String getAllOrders(Model model)
    {
        List<ProductOrder> allOrders = orderService.getAllOrders();
        model.addAttribute("orders",allOrders);
        model.addAttribute("srch", false);
        return "admin_order";
    }

    @GetMapping("/search-order")
    public String searchProduct(@RequestParam String orderId, Model m, RedirectAttributes session) {

        if (orderId != null && orderId.length() > 0) {

            ProductOrder order = orderService.getOrdersByOrderId(orderId.trim());

            if (ObjectUtils.isEmpty(order)) {
                session.addFlashAttribute("errorMsg","Incorrect order Id");
                m.addAttribute("orderDtls", null);
                return "redirect:/admin/orders";
            } else {
                m.addAttribute("orderDtls", order);
            }

            m.addAttribute("srch", true);
        } else {
            List<ProductOrder> allOrders = orderService.getAllOrders();
            m.addAttribute("orders", allOrders);
            m.addAttribute("srch", false);
        }
        return "admin_order";

    }

    @PostMapping("/update-order-status")
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
            session.addFlashAttribute("errorMsg", "status not updated");
        }
        return "redirect:/admin/orders";
    }




}
