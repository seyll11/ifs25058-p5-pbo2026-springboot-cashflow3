package org.delcom.ifs25058springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25058springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25058springbootcashflow.domain.entity.User;
import org.delcom.ifs25058springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25058springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25058springbootcashflow.usecase.UserUseCase;

/** Daftar seluruh pengguna - khusus ADMIN. */
@Route(value = "users", layout = MainLayout.class)
@PageTitle("Pengguna | Cashflow")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {

    private final IUserRepository users;
    private final UserUseCase userUseCase;
    private final Grid<User> grid = new Grid<>();

    public UsersView(IUserRepository users, UserUseCase userUseCase) {
        this.users = users;
        this.userUseCase = userUseCase;
        setSizeFull();

        GridStyles.apply(grid);
        grid.addColumn(User::id).setHeader("ID").setSortable(true).setWidth("80px").setFlexGrow(0);
        grid.addComponentColumn(u -> {
            Avatar avatar = new Avatar(u.fullName());
            String photo = FileStorage.urlOf(u.photoPath());
            if (photo != null) {
                avatar.setImage(photo);
            }
            HorizontalLayout row = new HorizontalLayout(avatar, new Span(u.fullName()));
            row.setAlignItems(Alignment.CENTER);
            return row;
        }).setHeader("Nama").setComparator(User::fullName).setSortable(true).setFlexGrow(2);
        grid.addColumn(User::username).setHeader("Username").setSortable(true);
        grid.addComponentColumn(u -> {
            Span badge = new Span(u.role().name());
            badge.addClassName(u.isAdmin() ? "cf-badge cf-badge-admin" : "cf-badge");
            return badge;
        }).setHeader("Role").setComparator(User::role).setSortable(true);
        grid.addComponentColumn(u -> {
            Button view = new Button("Lihat transaksi", VaadinIcon.EYE.create(), e ->
                    UI.getCurrent().navigate(UserTransactionsView.class,
                            QueryParameters.simple(java.util.Map.of("userId", Integer.toString(u.id())))));
            view.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            return view;
        }).setHeader("Tindakan").setFlexGrow(0).setWidth("200px");

        add(new H2("Daftar Pengguna"), grid);
        expand(grid);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = AuthSession.currentUser(users);
        if (user == null) {
            event.forwardTo(LoginView.class);
        } else if (!user.isAdmin()) {
            event.forwardTo(FinanceView.class);
        } else {
            grid.setItems(userUseCase.list());
        }
    }
}
