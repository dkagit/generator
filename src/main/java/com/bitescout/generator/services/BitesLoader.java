package com.bitescout.generator.services;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bitescout.generator.constants.BiteConstants;
import com.bitescout.generator.models.DishTime;
import com.bitescout.generator.models.DishType;
import com.bitescout.generator.models.Master;
import com.bitescout.generator.models.Master.Bite;
import com.bitescout.generator.models.Nature;
import com.google.gson.reflect.TypeToken;

import jakarta.annotation.PostConstruct;

@Service
public class BitesLoader {

    private final Logger logger = Logger.getLogger(BitesLoader.class.getName());
    private final ResourceLoader resourceLoader;
    private static final List<Master.Bite> BITES = new ArrayList<>();

    public BitesLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void loadBites() {
        try {
            logger.info("Loading bites master");
            Resource resource = resourceLoader.getResource("classpath:master.json");
            byte[] jsonBytes = Files.readAllBytes(Paths.get(resource.getURI()));
            String jsonString = new String(jsonBytes);
            Type lisType = new TypeToken<List<Master.Bite>>() {
            }.getType();
            BITES.addAll(BiteConstants.GSON.fromJson(jsonString, lisType));
        } catch (Exception e) {
            logger.severe("Error loading bites: " + e.getMessage());
        }
    }

    public static List<Master.Bite> getBites() {
        return BITES;
    }

    public void uploadBitesData(MultipartFile file) {
        try {
            Workbook workbook = WorkbookFactory.create(file.getInputStream());

            List<Bite> bites = new ArrayList<>();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                for (int j = 1; j < sheet.getPhysicalNumberOfRows(); j++) {
                    String dishName = getDishName(sheet.getRow(j).getCell(0));
                    if (StringUtils.isBlank(dishName)) {
                        continue;
                    }
                    Bite bite = new Bite(
                            dishName,
                            getLocalDishName(sheet.getRow(j).getCell(1)),
                            getDishTypeFromRawData(sheet.getRow(j).getCell(2)),
                            extractDishTime(sheet.getRow(j).getCell(3)),
                            processIngredients(sheet.getRow(j).getCell(4)),
                            getFoodNature(sheet.getRow(j).getCell(5)),
                            extractPreparationTime(sheet.getRow(j).getCell(6)),
                            extractBestServedDishTypes(sheet.getRow(j).getCell(7)));
                    bites.add(bite);
                }
            }
            logger.info("Uploading bites data {} records added".formatted(bites.size()));
            BITES.clear();
            BITES.addAll(bites);
            writeBitesToMaster();
        } catch (Exception e) {
            logger.severe("Error while uploading data: " + e.getMessage());
        }
    }

    private void writeBitesToMaster() {
        try {
            Resource resource = resourceLoader.getResource("classpath:master.json");
            Files.writeString(Paths.get(resource.getURI()), BiteConstants.GSON.toJson(BITES));
        } catch (Exception e) {
            logger.severe("Error writing bites to master: " + e.getMessage());
        }
    }

    private DishType getDishTypeFromRawData(Cell dishTypeCell) {
        if (dishTypeCell == null) {
            return DishType.GENERIC_BITE;
        }
        return getDishTypeFromRawData(dishTypeCell.getStringCellValue());
    }

    private DishType getDishTypeFromRawData(String dishType) {
        return switch (dishType) {
            case "Mains" -> DishType.MAINS;
            case "Light" -> DishType.LIGHTS;
            case "Biriyani" -> DishType.BIRIYANI;
            case "Chutney" -> DishType.CHUTNEY;
            case "Fried Rice" -> DishType.FRIED_RICE;
            case "Kulambu" -> DishType.KUZHAMBU;
            case "Kuruma" -> DishType.KURUMA;
            case "Poriyal" -> DishType.PORIYAL;
            case "Sambar" -> DishType.SAMBAR;
            case "Tiffin" -> DishType.TIFFIN;
            case "Variety Rice" -> DishType.VARIETY_RICE;
            default -> DishType.GENERIC_BITE;
        };
    }

    private HashSet<DishTime> extractDishTime(Cell dishTimeCell) {
        if (dishTimeCell == null) {
            return new HashSet<>();
        }
        if (StringUtils.isBlank(dishTimeCell.getStringCellValue())) {
            return new HashSet<>();
        }
        HashSet<DishTime> dishTimes = new LinkedHashSet<>();
        for (String split : dishTimeCell.getStringCellValue().split(",")) {
            split = split.trim();
            if (StringUtils.isNotBlank(split)) {
                if (split.equalsIgnoreCase("Breakfast")) {
                    dishTimes.add(DishTime.BREAKFAST);
                } else if (split.equalsIgnoreCase("Lunch")) {
                    dishTimes.add(DishTime.LUNCH);
                } else if (split.equalsIgnoreCase("Dinner")) {
                    dishTimes.add(DishTime.DINNER);
                }
            }
        }
        return dishTimes;
    }

    private LinkedHashSet<String> processIngredients(Cell ingredientsCell) {
        if (ingredientsCell == null) {
            return new LinkedHashSet<>();
        }
        if (StringUtils.isBlank(ingredientsCell.getStringCellValue())) {
            return new LinkedHashSet<>();
        }
        LinkedHashSet<String> ingredientSet = new LinkedHashSet<>();
        for (String split : ingredientsCell.getStringCellValue().split(",")) {
            split = split.trim();
            if (StringUtils.isNotBlank(split)) {
                ingredientSet.add(split);
            }
        }
        return ingredientSet;
    }

    private Nature getFoodNature(Cell natureCell) {
        if (natureCell == null) {
            return Nature.NON_VEG;
        }
        if (StringUtils.isBlank(natureCell.getStringCellValue())) {
            return Nature.NON_VEG;
        }

        if ("Non Veg".equalsIgnoreCase(natureCell.getStringCellValue())) {
            return Nature.NON_VEG;
        } else if ("Veg".equalsIgnoreCase(natureCell.getStringCellValue())) {
            return Nature.VEG;
        }

        return Nature.NON_VEG; // Default return value
    }

    private HashSet<DishType> extractBestServedDishTypes(Cell bestServedWithCell) {
        if (bestServedWithCell == null) {
            return new HashSet<>();
        }
        if (StringUtils.isBlank(bestServedWithCell.getStringCellValue())) {
            return new LinkedHashSet<>();
        }
        HashSet<DishType> dishTypes = new LinkedHashSet<>();
        for (String split : bestServedWithCell.getStringCellValue().split(",")) {
            split = split.trim();
            if (StringUtils.isNotBlank(split)) {
                dishTypes.add(getDishTypeFromRawData(split));
            }
        }
        return dishTypes;
    }

    private double extractPreparationTime(Cell preparationTimeCell) {
        if (preparationTimeCell == null) {
            return 120;
        }
        return preparationTimeCell.getNumericCellValue();
    }

    private String getDishName(Cell cell) {
        if (cell == null) {
            return "";
        }

        if (StringUtils.isBlank(cell.getStringCellValue())) {
            return "";
        }
        return cell.getStringCellValue();
    }

    private String getLocalDishName(Cell localName) {
        if (localName == null) {
            return "";
        }

        if (StringUtils.isBlank(localName.getStringCellValue())) {
            return "";
        }
        return localName.getStringCellValue();
    }

    public void clearBites() {
        BITES.clear();
    }
}
