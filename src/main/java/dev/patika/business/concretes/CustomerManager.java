package dev.patika.business.concretes;

import dev.patika.business.abstracts.ICustomerService;
import dev.patika.core.exception.EntityExistsException;
import dev.patika.core.exception.NotFoundException;
import dev.patika.core.utils.Message;
import dev.patika.dal.ICustomerRepo;
import dev.patika.dto.request.CustomerRequest;
import dev.patika.dto.response.standard.CustomerResponse;
import dev.patika.entity.Customer;
import dev.patika.core.config.mapper.ICustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerManager implements ICustomerService {

    private final ICustomerRepo customerRepo;
    private final ICustomerMapper customerMapper;

    // Değerlendirme Formu 18 - Girilen hayvan sahibinin sistemde kayıtlı tüm hayvanlarını görüntüleme
    // (sadece bir kişiye ait hayvanları görüntüle işlemi) başarılı bir şekilde çalışıyor mu?
    @Override
    public CustomerResponse getById(Long id) {
        return customerMapper.asOutput(customerRepo.findById(id).orElseThrow(() -> new NotFoundException(Message.NOT_FOUND)));
    }

    // Değerlendirme Formu 17 - Hayvan sahipleri isme göre filtreleniyor mu?
    // Değerlendirme Formu 18 - Girilen hayvan sahibinin sistemde kayıtlı tüm hayvanlarını görüntüleme
    // (sadece bir kişiye ait hayvanları görüntüle işlemi) başarılı bir şekilde çalışıyor mu?
    @Override
    public List<CustomerResponse> getByName(String name) {
        return customerMapper.asOutput(customerRepo.findByNameIgnoreCaseLike(name).orElseThrow(() -> new NotFoundException(Message.NOT_FOUND)));
    }

    // Değerlendirme Formu 10 - Proje isterlerine göre hayvan sahibi kaydediliyor mu?
    @Override
    public CustomerResponse create(CustomerRequest customerRequest) {
        Optional<Customer> isCustomerExist = customerRepo.findByPhoneOrMail(customerRequest.getPhone(), customerRequest.getMail());

        if (isCustomerExist.isEmpty()) {
            Customer customerSaved = customerRepo.save(customerMapper.asEntity(customerRequest));
            return customerMapper.asOutput(customerSaved);
        }
        throw new EntityExistsException(Message.ALREADY_EXIST);

    }


    @Override
    public void delete(Long id) {
        Optional<Customer> customerFromDb = customerRepo.findById(id);
        if (customerFromDb.isPresent()) {
            customerRepo.delete(customerFromDb.get());
        } else {
            throw new NotFoundException(Message.NOT_FOUND);
        }
    }

    @Override
    public CustomerResponse update(Long id, CustomerRequest request) {
        Optional<Customer> customerFromDb = customerRepo.findById(id);

        if (customerFromDb.isEmpty()) {
            throw new NotFoundException(Message.NOT_FOUND);
        }

        Customer customer = customerFromDb.get();
        customerMapper.update(customer, request);


        return customerMapper.asOutput(customerRepo.save(customer));
    }


    @Override
    public Page<CustomerResponse> cursor(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return this.customerRepo.findAll(pageable).map(this.customerMapper::asOutput);
    }
}
