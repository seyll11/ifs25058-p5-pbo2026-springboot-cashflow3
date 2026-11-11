package org.delcom.ifs00000springbootcashflow.framework.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;

/** Gaya grid yang konsisten di seluruh aplikasi. */
public final class GridStyles {

    private GridStyles() {
    }

    public static <T> void apply(Grid<T> grid) {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.addClassName("cf-grid");
        grid.setAllRowsVisible(false);
        grid.setMinHeight("320px");
        grid.setHeight("100%");
    }
}
