package org.delcom.ifs00000springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.List;
import org.delcom.ifs00000springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs00000springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs00000springbootcashflow.domain.entity.User;
import org.delcom.ifs00000springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs00000springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs00000springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs00000springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs00000springbootcashflow.usecase.UserUseCase;

/** Transaksi pengguna lain (read-only) - khusus ADMIN. Target dipilih lewat query ?userId=. */
@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna | Cashflow")
public class UserTransactionsView extends VerticalLayout implements BeforeEnterObserver {

    private final IUserRepository users;
    private final UserUseCase userUseCase;
    private final FinanceUseCase finance;
    private final FinancePresenter presenter;
    private final FileStorage storage;

    public UserTransactionsView(IUserRepository users, UserUseCase userUseCase, FinanceUseCase finance,
                                FinancePresenter presenter, FileStorage storage) {
        this.users = users;
        this.userUseCase = userUseCase;
        this.finance = finance;
        this.presenter = presenter;
        this.storage = storage;
        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User admin = AuthSession.currentUser(users);
        if (admin == null) {
            event.forwardTo(LoginView.class);
            return;
        }
        if (!admin.isAdmin()) {
            event.forwardTo(FinanceView.class);
            return;
        }
        List<String> ids = event.getLocation().getQueryParameters().getParameters().get("userId");
        try {
            int targetId = ids == null || ids.isEmpty() ? 0 : Integer.parseInt(ids.get(0));
            User target = userUseCase.get(targetId);
            removeAll();
            Button back = new Button("Kembali ke daftar pengguna", VaadinIcon.ARROW_LEFT.create(),
                    e -> UI.getCurrent().navigate(UsersView.class));
            add(back, new H2("Transaksi: " + target.fullName() + " (" + target.username() + ") - hanya lihat"),
                    new TransactionPanel(finance, presenter, storage, admin, target.id(), true));
        } catch (NumberFormatException e) {
            Notifier.error("ID pengguna tidak valid");
            event.forwardTo(UsersView.class);
        } catch (DomainException e) {
            Notifier.error(presenter.errorMessage(e));
            event.forwardTo(UsersView.class);
        }
    }
}
