package org.delcom.ifs25058springbootcashflow.framework.view;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLayout;
import org.delcom.ifs25058springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25058springbootcashflow.domain.entity.User;
import org.delcom.ifs25058springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25058springbootcashflow.framework.auth.AuthSession;

/** Layout halaman terproteksi: sidebar, menu avatar, dan pengecekan login global. */
public class MainLayout extends AppLayout implements RouterLayout, BeforeEnterObserver {

    private final IUserRepository users;
    private final SideNav nav = new SideNav("Menu");
    private final HorizontalLayout account = new HorizontalLayout();

    public MainLayout(IUserRepository users) {
        this.users = users;

        DrawerToggle toggle = new DrawerToggle();
        toggle.getElement().setAttribute("aria-label", "Buka atau tutup menu navigasi");
        H1 title = new H1("Cashflow");
        title.addClassName("cf-app-title");

        account.setAlignItems(FlexComponent.Alignment.CENTER);
        account.addClassName("cf-account");

        HorizontalLayout bar = new HorizontalLayout(toggle, title, account);
        bar.setWidthFull();
        bar.setAlignItems(FlexComponent.Alignment.CENTER);
        bar.expand(title);
        bar.addClassName("cf-navbar");
        // Landmark aksesibilitas: navbar = banner, drawer = navigation, konten = main.
        bar.getElement().setAttribute("role", "banner");
        addToNavbar(bar);

        nav.getElement().setAttribute("role", "navigation");
        addToDrawer(nav);
        setPrimarySection(Section.DRAWER);
    }

    /** Menandai konten halaman aktif sebagai landmark utama (satu elemen main per dokumen). */
    @Override
    public void showRouterLayoutContent(HasElement content) {
        super.showRouterLayoutContent(content);
        if (content != null) {
            content.getElement().setAttribute("role", "main");
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = AuthSession.currentUser(users);
        if (user == null) {
            event.forwardTo(LoginView.class);
            return;
        }
        render(user);
    }

    private void render(User user) {
        nav.removeAll();
        nav.addItem(new SideNavItem("Transaksi", FinanceView.class, VaadinIcon.WALLET.create()));
        if (user.isAdmin()) {
            nav.addItem(new SideNavItem("Pengguna", UsersView.class, VaadinIcon.USERS.create()));
        }
        nav.addItem(new SideNavItem("Profil", ProfileView.class, VaadinIcon.USER.create()));

        Avatar avatar = new Avatar(user.fullName());
        String photo = FileStorage.urlOf(user.photoPath());
        if (photo != null) {
            avatar.setImage(photo);
        }
        MenuBar menu = new MenuBar();
        menu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
        var root = menu.addItem(avatar);
        root.getElement().setAttribute("aria-label", "Menu akun " + user.fullName());
        root.getSubMenu().addItem("Profil", e -> UI.getCurrent().navigate(ProfileView.class));
        root.getSubMenu().addItem("Keluar", e -> logout());

        Span name = new Span(user.fullName());
        name.addClassName("cf-account-name");
        Span role = new Span(user.role().name());
        role.addClassNames(user.isAdmin() ? "cf-badge cf-badge-admin" : "cf-badge");

        account.removeAll();
        account.add(name, role, menu);
    }

    private void logout() {
        AuthSession.logout();
        UI.getCurrent().navigate(LoginView.class);
    }
}
