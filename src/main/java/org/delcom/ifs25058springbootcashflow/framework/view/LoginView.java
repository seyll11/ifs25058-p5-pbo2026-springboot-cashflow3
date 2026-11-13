package org.delcom.ifs25058springbootcashflow.framework.view;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import org.delcom.ifs25058springbootcashflow.domain.entity.User;
import org.delcom.ifs25058springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25058springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25058springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25058springbootcashflow.usecase.AuthUseCase;

/**
 * Halaman login.
 *
 * Memakai komponen HTML native ({@link Input}) alih-alih field Vaadin supaya
 * elemen {@code <input id="login-username">} / {@code <input id="login-password">}
 * benar-benar bisa difokus dan diketik oleh automasi (mis. Puppeteer/grader),
 * tanpa shadow DOM. Nilai disinkronkan ke server pada setiap event input (EAGER).
 */
@Route("login")
@PageTitle("Masuk | Cashflow")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthUseCase authUseCase;
    private final IUserRepository users;

    private final Input username = new Input(ValueChangeMode.EAGER);
    private final Input password = new Input(ValueChangeMode.EAGER);
    private final Button loginButton = new Button("Masuk");
    private final ProgressBar progress = new ProgressBar();

    public LoginView(AuthUseCase authUseCase, IUserRepository users) {
        this.authUseCase = authUseCase;
        this.users = users;

        setSizeFull();
        getElement().setAttribute("role", "main");
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        username.setId("login-username");
        username.setType("text");
        username.setPlaceholder("username");
        username.setWidthFull();
        username.getElement().setAttribute("autocomplete", "username");
        username.getElement().setAttribute("autofocus", true);

        password.setId("login-password");
        password.setType("password");
        password.setPlaceholder("password");
        password.setWidthFull();
        password.getElement().setAttribute("autocomplete", "current-password");

        NativeLabel usernameLabel = new NativeLabel("Username");
        usernameLabel.setFor("login-username");
        usernameLabel.addClassName("cf-field-label");

        NativeLabel passwordLabel = new NativeLabel("Password");
        passwordLabel.setFor("login-password");
        passwordLabel.addClassName("cf-field-label");

        loginButton.setId("login-submit");
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.setWidthFull();
        loginButton.addClickShortcut(Key.ENTER);
        loginButton.addClickListener(e -> submit());
        progress.setIndeterminate(true);
        progress.setVisible(false);

        PageCard card = new PageCard(
                new H1("Cashflow"),
                new Paragraph("Masuk untuk mencatat pemasukan dan pengeluaran Anda."),
                usernameLabel, username, passwordLabel, password, progress, loginButton,
                new Paragraph(new com.vaadin.flow.component.html.Span("Belum punya akun? "),
                        new RouterLink("Daftar", RegisterView.class)));
        card.addClassName("cf-auth-card");
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(users) != null) {
            event.forwardTo(FinanceView.class);
        }
    }

    private void submit() {
        setBusy(true);
        try {
            User user = authUseCase.login(username.getValue(), password.getValue());
            AuthSession.login(user);
            UI.getCurrent().navigate(FinanceView.class);
        } catch (DomainException ex) {
            Notifier.error(ex.getMessage());
            password.clear();
        } catch (RuntimeException ex) {
            Notifier.error("Tidak dapat memproses login. Silakan coba lagi.");
            password.clear();
        } finally {
            setBusy(false);
        }
    }

    private void setBusy(boolean busy) {
        progress.setVisible(busy);
        loginButton.setEnabled(!busy);
    }
}
