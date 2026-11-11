package org.delcom.ifs00000springbootcashflow.framework.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs00000springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs00000springbootcashflow.domain.entity.User;
import org.delcom.ifs00000springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs00000springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs00000springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs00000springbootcashflow.usecase.UserUseCase;

/** Pengaturan akun: nama, password, foto profil. */
@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil | Cashflow")
public class ProfileView extends VerticalLayout implements BeforeEnterObserver {

    private final IUserRepository users;
    private final UserUseCase userUseCase;
    private final FileStorage storage;

    public ProfileView(IUserRepository users, UserUseCase userUseCase, FileStorage storage) {
        this.users = users;
        this.userUseCase = userUseCase;
        this.storage = storage;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        User user = AuthSession.currentUser(users);
        if (user == null) {
            event.forwardTo(LoginView.class);
            return;
        }
        removeAll();

        Avatar avatar = new Avatar(user.fullName());
        avatar.setHeight("96px");
        avatar.setWidth("96px");
        String photo = FileStorage.urlOf(user.photoPath());
        if (photo != null) {
            avatar.setImage(photo);
        }
        TextField username = new TextField("Username");
        username.setValue(user.username());
        username.setReadOnly(true);
        username.setWidthFull();
        TextField fullName = new TextField("Nama lengkap");
        fullName.setValue(user.fullName());
        fullName.setWidthFull();
        PasswordField password = new PasswordField("Password baru");
        password.setHelperText("Kosongkan bila tidak ingin mengganti password (minimal 6 karakter)");
        password.setWidthFull();
        ImageUploadField upload = new ImageUploadField();
        Paragraph hint = new Paragraph("Foto profil (opsional): jpg, png, gif, atau webp, maks. 5 MB.");
        hint.addClassName("cf-hint");

        ProgressBar progress = new ProgressBar();
        progress.setIndeterminate(true);
        progress.setVisible(false);
        Button save = new Button("Simpan perubahan");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            progress.setVisible(true);
            save.setEnabled(false);
            try {
                saveProfile(user, fullName.getValue(), password.getValue(), upload);
            } finally {
                progress.setVisible(false);
                save.setEnabled(true);
            }
        });

        PageCard card = new PageCard(new H2("Profil Saya"), avatar, username, fullName, password,
                hint, upload, progress, save);
        card.addClassName("cf-profile-card");
        add(card);
    }

    private void saveProfile(User user, String fullName, String newPassword, ImageUploadField upload) {
        String newPhoto = null;
        try {
            userUseCase.updateProfile(user.id(), fullName, newPassword);
            if (upload.hasFile()) {
                newPhoto = storage.store(upload.data(), upload.fileName());
                userUseCase.updatePhoto(user.id(), newPhoto);
                storage.delete(user.photoPath());
            }
            Notifier.success("Profil berhasil diperbarui");
            UI.getCurrent().getPage().reload();
        } catch (DomainException ex) {
            if (newPhoto != null) {
                storage.delete(newPhoto);
            }
            Notifier.error(ex.getMessage());
        }
    }
}
