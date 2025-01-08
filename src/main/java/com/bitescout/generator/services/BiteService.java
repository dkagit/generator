package com.bitescout.generator.services;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.bitescout.generator.models.BitesForPlate;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bitescout.generator.models.DishType;
import com.bitescout.generator.models.Master.Bite;

@Service
public class BiteService {
    public final Logger logger = Logger.getLogger(BiteService.class.getName());
    public final BitesLoader bitesLoader;

    public static final List<DishType> STANDARD_DISH_TYPES_FOR_PLATE = List.of(
            DishType.MAINS,
            DishType.BIRIYANI,
            DishType.FRIED_RICE,
            DishType.VARIETY_RICE,
            DishType.TIFFIN
    );

    public static final List<Pair<DishType, List<DishType>>> COMBO_LIST = List.of(
            Pair.of(DishType.MAINS, List.of(DishType.KURUMA, DishType.PORIYAL)),
            Pair.of(DishType.MAINS, List.of(DishType.SAMBAR, DishType.PORIYAL)),
            Pair.of(DishType.VARIETY_RICE, List.of(DishType.PORIYAL)),
            Pair.of(DishType.TIFFIN, List.of(DishType.CHUTNEY)),
            Pair.of(DishType.TIFFIN, List.of(DishType.SAMBAR)),
            Pair.of(DishType.TIFFIN, List.of(DishType.KUZHAMBU)),
            Pair.of(DishType.TIFFIN, List.of(DishType.KURUMA))
    );

    public BiteService(BitesLoader bitesLoader) {
        this.bitesLoader = bitesLoader;
    }

    public List<Bite> getMasterData() {
        return BitesLoader.getBites();
    }

    public void uploadBitesData(MultipartFile file) {
        logger.info("Uploading bites data");
        bitesLoader.uploadBitesData(file);
    }

    public List<Bite> searchBites(DishType dishType) {
        return getMasterData().stream().filter(bite -> bite.dishType().equals(dishType)).collect(Collectors.toList());
    }

    public Pair<DishType, List<DishType>> getRandomCombo() {
        return COMBO_LIST.get((int) (Math.random() * COMBO_LIST.size()));
    }

    public Bite getRandomBite(DishType dishType) {
        List<Bite> bites = searchBites(dishType);
        return bites.get((int) (Math.random() * bites.size()));
    }

    public BitesForPlate serveBite() {
        var bitesForPlate = new BitesForPlate();

        Pair<DishType, List<DishType>> combo = getRandomCombo();
        Bite heavyBite = getRandomBite(combo.getLeft());
        bitesForPlate.getBites().add(heavyBite);

        List<Bite> otherBites = combo.getRight().stream().map(this::getRandomBite).toList();
        bitesForPlate.getBites().addAll(otherBites);
        return bitesForPlate;
    }

    public void clearAllBitesData() {
        bitesLoader.clearBites();
    }
}
