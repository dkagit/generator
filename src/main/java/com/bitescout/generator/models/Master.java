package com.bitescout.generator.models;

import java.util.HashSet;
import java.util.LinkedHashSet;

public class Master {
    public record Bite(
        String dishName,
        String localName,
        DishType dishType,
        HashSet<DishTime> dishTime,
        LinkedHashSet<String> ingredients,
        Nature nature,
        double preparationTime,
        HashSet<DishType> bestServedWith
    ) {}
}
