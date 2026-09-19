package io.github.m1jawa.mcmodsmanager.model;

public record SearchedModData(String id, String name, ModLoader modLoader, int downloads, String description) implements IModData {}