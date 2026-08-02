package io.github.m1jawa.mcmodsmanager.model;

import java.util.List;

public record LoadedModsData(int totalMods, int succesCount, int failedCount, List<ModData> failedMods) {}