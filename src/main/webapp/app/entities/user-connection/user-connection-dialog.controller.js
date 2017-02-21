(function() {
    'use strict';

    angular
        .module('backendApp')
        .controller('UserConnectionDialogController', UserConnectionDialogController);

    UserConnectionDialogController.$inject = ['$timeout', '$scope', '$stateParams', '$uibModalInstance', 'entity', 'UserConnection', 'User'];

    function UserConnectionDialogController ($timeout, $scope, $stateParams, $uibModalInstance, entity, UserConnection, User) {
        var vm = this;

        vm.userConnection = entity;
        vm.clear = clear;
        vm.save = save;
        vm.users = User.query();

        $timeout(function (){
            angular.element('.form-group:eq(1)>input').focus();
        });

        function clear () {
            $uibModalInstance.dismiss('cancel');
        }

        function save () {
            vm.isSaving = true;
            if (vm.userConnection.id !== null) {
                UserConnection.update(vm.userConnection, onSaveSuccess, onSaveError);
            } else {
                UserConnection.save(vm.userConnection, onSaveSuccess, onSaveError);
            }
        }

        function onSaveSuccess (result) {
            $scope.$emit('backendApp:userConnectionUpdate', result);
            $uibModalInstance.close(result);
            vm.isSaving = false;
        }

        function onSaveError () {
            vm.isSaving = false;
        }


    }
})();
