package com.bitescout.generator.models;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BitesForPlate {
    public List<Master.Bite> bites = new ArrayList<>(5);
}
