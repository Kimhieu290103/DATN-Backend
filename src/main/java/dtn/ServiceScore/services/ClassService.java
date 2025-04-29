package dtn.ServiceScore.services;

import dtn.ServiceScore.dtos.ClassDTO;
import dtn.ServiceScore.dtos.ClassMoDTO;
import dtn.ServiceScore.dtos.ClassSearchRequest;
import dtn.ServiceScore.model.Class;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ClassService {
    List<Class> getAllClass();

    List<Class> getClasses(ClassSearchRequest request);

    void createClass(ClassDTO classDTO);

     Class updateClass(Long id, ClassMoDTO classDTO);

    Class updateClassStatusToFalse(Long classId);

    Page<Class> getAllClassPagedSorted(int page, int size);


}
