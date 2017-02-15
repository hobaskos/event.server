(function() {
    'use strict';

    angular
        .module('backendApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider'];

    function stateConfig($stateProvider) {
        $stateProvider
        .state('event-category', {
            parent: 'entity',
            url: '/event-category?page&sort&search',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'backendApp.eventCategory.home.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/event-category/event-categories.html',
                    controller: 'EventCategoryController',
                    controllerAs: 'vm'
                }
            },
            params: {
                page: {
                    value: '1',
                    squash: true
                },
                sort: {
                    value: 'id,asc',
                    squash: true
                },
                search: null
            },
            resolve: {
                pagingParams: ['$stateParams', 'PaginationUtil', function ($stateParams, PaginationUtil) {
                    return {
                        page: PaginationUtil.parsePage($stateParams.page),
                        sort: $stateParams.sort,
                        predicate: PaginationUtil.parsePredicate($stateParams.sort),
                        ascending: PaginationUtil.parseAscending($stateParams.sort),
                        search: $stateParams.search
                    };
                }],
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('eventCategory');
                    $translatePartialLoader.addPart('eventCategoryTheme');
                    $translatePartialLoader.addPart('global');
                    return $translate.refresh();
                }]
            }
        })
        .state('event-category-detail', {
            parent: 'entity',
            url: '/event-category/{id}',
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'backendApp.eventCategory.detail.title'
            },
            views: {
                'content@': {
                    templateUrl: 'app/entities/event-category/event-category-detail.html',
                    controller: 'EventCategoryDetailController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('eventCategory');
                    $translatePartialLoader.addPart('eventCategoryTheme');
                    return $translate.refresh();
                }],
                entity: ['$stateParams', 'EventCategory', function($stateParams, EventCategory) {
                    return EventCategory.get({id : $stateParams.id}).$promise;
                }],
                previousState: ["$state", function ($state) {
                    var currentStateData = {
                        name: $state.current.name || 'event-category',
                        params: $state.params,
                        url: $state.href($state.current.name, $state.params)
                    };
                    return currentStateData;
                }]
            }
        })
        .state('event-category-detail.edit', {
            parent: 'event-category-detail',
            url: '/detail/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/event-category/event-category-dialog.html',
                    controller: 'EventCategoryDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['EventCategory', function(EventCategory) {
                            return EventCategory.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('^', {}, { reload: false });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('event-category.new', {
            parent: 'event-category',
            url: '/new',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/event-category/event-category-dialog.html',
                    controller: 'EventCategoryDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: function () {
                            return {
                                title: null,
                                icon: null,
                                iconContentType: null,
                                theme: null,
                                id: null
                            };
                        }
                    }
                }).result.then(function() {
                    $state.go('event-category', null, { reload: 'event-category' });
                }, function() {
                    $state.go('event-category');
                });
            }]
        })
        .state('event-category.edit', {
            parent: 'event-category',
            url: '/{id}/edit',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/event-category/event-category-dialog.html',
                    controller: 'EventCategoryDialogController',
                    controllerAs: 'vm',
                    backdrop: 'static',
                    size: 'lg',
                    resolve: {
                        entity: ['EventCategory', function(EventCategory) {
                            return EventCategory.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('event-category', null, { reload: 'event-category' });
                }, function() {
                    $state.go('^');
                });
            }]
        })
        .state('event-category.delete', {
            parent: 'event-category',
            url: '/{id}/delete',
            data: {
                authorities: ['ROLE_USER']
            },
            onEnter: ['$stateParams', '$state', '$uibModal', function($stateParams, $state, $uibModal) {
                $uibModal.open({
                    templateUrl: 'app/entities/event-category/event-category-delete-dialog.html',
                    controller: 'EventCategoryDeleteController',
                    controllerAs: 'vm',
                    size: 'md',
                    resolve: {
                        entity: ['EventCategory', function(EventCategory) {
                            return EventCategory.get({id : $stateParams.id}).$promise;
                        }]
                    }
                }).result.then(function() {
                    $state.go('event-category', null, { reload: 'event-category' });
                }, function() {
                    $state.go('^');
                });
            }]
        });
    }

})();
