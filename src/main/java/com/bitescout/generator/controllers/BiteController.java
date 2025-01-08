package com.bitescout.generator.controllers;

import com.bitescout.generator.constants.BiteConstants;
import com.bitescout.generator.exceptions.BiteException;
import com.bitescout.generator.models.DishType;
import com.bitescout.generator.models.Master.Bite;
import com.bitescout.generator.models.ResponseMaster.GenericResponse;
import com.bitescout.generator.models.ResponseMaster.Status;
import com.bitescout.generator.services.BiteService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.logging.Logger;


@RestController
@RequestMapping("/bite")
public class BiteController {

    private static final Logger logger = Logger.getLogger(BiteController.class.getName());
    private final BiteService biteService;

    public BiteController(BiteService biteService) {
        this.biteService = biteService;
    }

    //Health check api
    @GetMapping()
    public String healthCheck() {
        logger.info("Health check api called");
        return "Meals Ready";
    }

    @PostMapping("/upload")
    public ResponseEntity<GenericResponse> uploadBiteData(
            @RequestPart("file") MultipartFile file
    ) {
        logger.info("Uploading bite data");
        biteService.uploadBitesData(file);
        return ResponseEntity.ok(new GenericResponse("Data uploaded successfully", Status.SUCCESS,null));
    }

    @GetMapping("/all")
    public ResponseEntity<GenericResponse> getAllBites() {
        logger.info("Fetching all bites");
        try {
            return ResponseEntity.ok(new GenericResponse(biteService.getMasterData(), Status.SUCCESS, null));
        } catch (Exception e) {
            return ResponseEntity.ok(new GenericResponse("Error fetching bites", Status.FAILURE, null));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<GenericResponse> searchBites(
            @RequestParam DishType dishType
    ) {
        logger.info("Searching bites");
        try {
            return ResponseEntity.ok(new GenericResponse(biteService.searchBites(dishType), Status.SUCCESS, null));
        } catch (Exception e) {
            return ResponseEntity.ok(new GenericResponse("Error fetching bites", Status.FAILURE, null));
        }
    }

    @GetMapping("/clear/all")
    public ResponseEntity<GenericResponse> clearAllBitesData() {
        logger.info("Clearing all bites data");
        try {
            biteService.clearAllBitesData();
            return ResponseEntity.ok(new GenericResponse("All bites data cleared", Status.SUCCESS, null));
        } catch (Exception e) {
            logger.severe("Error clearing bites data: " + e.getMessage());
            return ResponseEntity.ok(new GenericResponse("Error clearing bites data", Status.FAILURE, null));
        }
    }

    @GetMapping("/serve")
    public ResponseEntity<GenericResponse> serveBites() {
        logger.info("Serving bites");
        try {
            var dish = biteService.serveBite();
            return ResponseEntity.ok(new GenericResponse(dish, Status.SUCCESS, null));
        } catch (BiteException e) {
            return ResponseEntity.ok(new GenericResponse(e.getMessage(), Status.FAILURE, null));
        } catch (Exception e) {
            e.printStackTrace();
            String error = e.getMessage();
            return ResponseEntity.ok(new GenericResponse(BiteConstants.ERROR_SERVING_BITE, Status.FAILURE, error));
        }
    }
}
