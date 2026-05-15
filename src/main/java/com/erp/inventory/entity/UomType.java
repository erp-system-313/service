package com.erp.inventory.entity;

/**
 * UoM Type — categorizes units by what they measure.
 */
public enum UomType {
    /** Countable items (pieces, units, dozens). */
    UNIT,
    /** Length (meters, feet, inches). */
    LENGTH,
    /** Weight/mass (kg, lb, oz). */
    WEIGHT,
    /** Volume (liters, gallons, cubic meters). */
    VOLUME,
    /** Time (hours, days, minutes). */
    TIME,
    /** Area (square meters, acres). */
    AREA,
    /** Working time (for services). */
    WORKING_HOURS
}
