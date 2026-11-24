package com.pedro.WebHooksAlerts.monitor;

import com.pedro.WebHooksAlerts.monitor.dtos.CreateMonitorRequest;
import com.pedro.WebHooksAlerts.monitor.dtos.MonitorResponse;

import com.pedro.WebHooksAlerts.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MonitorService {

    private final MonitorRepository monitorRepository;

    @Transactional
    public MonitorResponse create(CreateMonitorRequest request){
        Monitor monitor = new Monitor(request.name(),request.targetUrl(),request.intervalSeconds(),request.timeoutMs());
        return MonitorResponse.from(monitorRepository.save(monitor));
    }

    @Transactional(readOnly = true)
    public List<MonitorResponse> findAll(){
        return monitorRepository.findAll().stream().map(MonitorResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public MonitorResponse findById(UUID id){
        Monitor monitor = monitorRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Monitor Not Found " + id));
        return MonitorResponse.from(monitor);
    }

    @Transactional
    public MonitorResponse updateActiveStatus(UUID id, boolean active){
        Monitor monitor = monitorRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Monitor Not Found " + id));
        if(active){
            monitor.resume();
        }else{
            monitor.pause();
        }
        return MonitorResponse.from(monitorRepository.save(monitor));
    }

    @Transactional
    public void delete(UUID id){
        Monitor monitor = monitorRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Monitor Not Found " + id));
        monitorRepository.delete(monitor);
    }

    @Transactional(readOnly = true)
    public void ensureExists(UUID monitorId){
        if (!monitorRepository.existsById(monitorId)){
            throw new ResourceNotFoundException("Monitor not Found: " + monitorId);
        }
    }

}
