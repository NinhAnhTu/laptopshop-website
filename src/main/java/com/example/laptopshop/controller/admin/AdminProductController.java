package com.example.laptopshop.controller.admin;

import com.example.laptopshop.dto.request.ProductCreateDTO;
import com.example.laptopshop.entity.Product;
import com.example.laptopshop.repository.*;
import com.example.laptopshop.service.BrandService;
import com.example.laptopshop.service.CategoryService;
import com.example.laptopshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;

    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final WarrantyPolicyRepository warrantyPolicyRepository;

    // --- 1. DANH SÁCH SẢN PHẨM ---
    @GetMapping("/products")
    public String listProducts(Model model,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Long categoryId,
                               @RequestParam(required = false) Long brandId,
                               @RequestParam(required = false) Double minPrice,
                               @RequestParam(required = false) Double maxPrice) {

        // Gọi hàm tìm kiếm
        List<Product> products = productService.searchProducts(keyword, categoryId, brandId, minPrice, maxPrice);
        model.addAttribute("products", products);

        // Đổ dữ liệu vào Dropdown bộ lọc
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("brands", brandService.getAllBrands());

        // Giữ lại giá trị filter để hiện lại trên form
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedBrandId", brandId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        // Active menu sidebar (nếu có)
        model.addAttribute("activePage", "products");

        return "admin/product/list";
    }

    // --- 2. CÁC HÀM THÊM / SỬA / XÓA ---

    @GetMapping("/products/create")
    public String showCreateForm(Model model) {
        model.addAttribute("productDTO", new ProductCreateDTO());
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("suppliers", supplierRepository.findAll());
        model.addAttribute("warranties", warrantyPolicyRepository.findAll());
        return "admin/product/create";
    }

    @PostMapping("/products/create")
    public String createProduct(@ModelAttribute ProductCreateDTO productDTO) {
        productService.createProduct(productDTO);
        return "redirect:/admin/products";
    }

    @GetMapping("/products/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product == null) return "redirect:/admin/products";

        ProductCreateDTO dto = new ProductCreateDTO();
        dto.setProductName(product.getProductName());
        dto.setOriginalPrice(product.getOriginalPrice());
        dto.setSalePrice(product.getSalePrice());
        dto.setStock(product.getStock());
        dto.setCpu(product.getCpu());
        dto.setRam(product.getRam());
        dto.setStorage(product.getStorage());
        dto.setGpu(product.getGpu());
        dto.setScreen(product.getScreen());

        if (product.getBrand() != null) dto.setBrandId(product.getBrand().getBrandId());
        if (product.getCategory() != null) dto.setCategoryId(product.getCategory().getCategoryId());
        if (product.getSupplier() != null) dto.setSupplierId(product.getSupplier().getSupplierId());
        if (product.getWarrantyPolicy() != null) dto.setWarrantyPolicyId(product.getWarrantyPolicy().getWarrantyId());

        model.addAttribute("productDTO", dto);
        model.addAttribute("productId", id);
        model.addAttribute("brands", brandRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("suppliers", supplierRepository.findAll());
        model.addAttribute("warranties", warrantyPolicyRepository.findAll());

        return "admin/product/edit";
    }

    @PostMapping("/products/update/{id}")
    public String updateProduct(@PathVariable Long id, @ModelAttribute ProductCreateDTO productDTO) {
        productService.updateProduct(id, productDTO);
        return "redirect:/admin/products";
    }

    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
        } catch (Exception e) {
            System.out.println("Lỗi khi xóa: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }
}