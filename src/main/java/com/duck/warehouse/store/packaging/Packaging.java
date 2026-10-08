package com.duck.warehouse.store.packaging;

import java.util.List;

import com.duck.warehouse.store.domain.*;

public record Packaging(PackageType packageType, List<ProtectionType> protections) {
}
